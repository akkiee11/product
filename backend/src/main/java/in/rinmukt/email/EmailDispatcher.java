package in.rinmukt.email;

import in.rinmukt.domain.Report;
import in.rinmukt.persistence.MriPersistenceService;
import in.rinmukt.persistence.MriSessionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Drives the two outbound emails:
 *   1. Welcome — fired async right after a session is persisted.
 *   2. Day-7 follow-up — fired by a cron that runs every hour.
 *
 * Both reads and writes go through MriPersistenceService so this works
 * even when JPA is excluded (dev profile) — the calls just no-op.
 */
@Component
public class EmailDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EmailDispatcher.class);

    private final EmailService emailService;
    private final MriPersistenceService persistence;

    public EmailDispatcher(EmailService emailService, MriPersistenceService persistence) {
        this.emailService = emailService;
        this.persistence = persistence;
    }

    /** Fire-and-forget welcome from POST /api/mri. */
    @Async
    public void dispatchWelcome(UUID sessionId, String email, Report report) {
        if (sessionId == null || email == null || email.isBlank()) return;
        if (!emailService.isEnabled()) return;
        boolean ok = emailService.sendWelcome(email, sessionId, report);
        if (ok) persistence.markWelcomeSent(sessionId);
    }

    /**
     * Runs every hour at :05 past. Pulls every session whose welcome
     * was sent ≥ 7 days ago and that hasn't received the follow-up.
     * Cron uses the JVM's zone; that's fine because we compare on
     * createdAt (stored as TIMESTAMPTZ) using UTC.
     */
    @Scheduled(cron = "0 5 * * * *")
    public void runDay7FollowUpsHourly() {
        if (!emailService.isEnabled()) return;
        OffsetDateTime sevenDaysAgo = OffsetDateTime.now(ZoneOffset.UTC).minus(Duration.ofDays(7));
        List<MriSessionEntity> due = persistence.findDay7Due(sevenDaysAgo);
        if (due.isEmpty()) return;
        log.info("Day-7 follow-up job: {} sessions due", due.size());
        for (MriSessionEntity s : due) {
            try {
                Report r = s.getReportJson();
                if (r == null) continue;
                boolean ok = emailService.sendDay7FollowUp(s.getEmail(), s.getId(), r);
                if (ok) persistence.markDay7Sent(s.getId());
            } catch (Exception e) {
                log.warn("Day-7 send skipped for session {}: {}", s.getId(), e.getMessage());
            }
        }
    }
}
