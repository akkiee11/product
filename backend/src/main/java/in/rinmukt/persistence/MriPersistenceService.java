package in.rinmukt.persistence;

import in.rinmukt.domain.Report;
import in.rinmukt.dto.MriRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persists MRI sessions when a JPA repository is wired (prod profile).
 * In dev profile JPA is excluded, so the repository bean is absent and
 * every call quietly no-ops — the controller still serves the report
 * out of memory, just without an id.
 */
@Service
public class MriPersistenceService {

    private final ObjectProvider<MriSessionRepository> repoProvider;

    public MriPersistenceService(ObjectProvider<MriSessionRepository> repoProvider) {
        this.repoProvider = repoProvider;
    }

    public Optional<UUID> save(MriRequest req, Report report) {
        MriSessionRepository repo = repoProvider.getIfAvailable();
        if (repo == null) return Optional.empty();

        MriSessionEntity entity = new MriSessionEntity();
        entity.setRequestJson(req);
        entity.setReportJson(report);
        entity.setHealthScore(report.getHealthScore());
        entity.setRecommendedPath(report.getRecommendedPathId());
        // Email is captured at submission, not on the report.
        if (req.email() != null && !req.email().isBlank()) {
            entity.setEmail(req.email().trim());
        }
        return Optional.of(repo.save(entity).getId());
    }

    public Optional<Report> findReport(UUID id) {
        MriSessionRepository repo = repoProvider.getIfAvailable();
        if (repo == null) return Optional.empty();
        return repo.findById(id).map(e -> {
            Report r = e.getReportJson();
            if (r != null) r.setId(e.getId());
            return r;
        });
    }

    public Optional<MriSessionEntity> findEntity(UUID id) {
        MriSessionRepository repo = repoProvider.getIfAvailable();
        if (repo == null) return Optional.empty();
        return repo.findById(id);
    }

    @Transactional
    public void markWelcomeSent(UUID id) {
        MriSessionRepository repo = repoProvider.getIfAvailable();
        if (repo == null) return;
        repo.findById(id).ifPresent(e -> {
            e.setWelcomeSentAt(OffsetDateTime.now(ZoneOffset.UTC));
            repo.save(e);
        });
    }

    @Transactional
    public void markDay7Sent(UUID id) {
        MriSessionRepository repo = repoProvider.getIfAvailable();
        if (repo == null) return;
        repo.findById(id).ifPresent(e -> {
            e.setDay7SentAt(OffsetDateTime.now(ZoneOffset.UTC));
            repo.save(e);
        });
    }

    public List<MriSessionEntity> findDay7Due(OffsetDateTime before) {
        MriSessionRepository repo = repoProvider.getIfAvailable();
        if (repo == null) return List.of();
        return repo.findDay7Due(before);
    }

    public List<MriSessionEntity> findRecent(int limit) {
        MriSessionRepository repo = repoProvider.getIfAvailable();
        if (repo == null) return List.of();
        return repo.findAll(org.springframework.data.domain.PageRequest.of(0, limit,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        )).getContent();
    }

    public long countAll() {
        MriSessionRepository repo = repoProvider.getIfAvailable();
        return repo == null ? 0 : repo.count();
    }
}
