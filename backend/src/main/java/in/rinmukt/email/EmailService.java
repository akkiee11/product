package in.rinmukt.email;

import in.rinmukt.domain.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Sends transactional emails via Gmail SMTP.
 *
 * If SMTP isn't configured (no JavaMailSender bean, or no FROM address)
 * every send() is a silent no-op so dev mode + un-configured prod still
 * function. The MriController never blocks on email delivery — see the
 * @Async dispatch.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromAddress;
    private final String publicBaseUrl;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${rinmukt.email.from:}") String fromAddress,
            @Value("${rinmukt.email.public-base-url:https://rinmukt.vercel.app}") String publicBaseUrl
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.fromAddress = fromAddress;
        this.publicBaseUrl = publicBaseUrl;
    }

    /** True when both an SMTP sender and a FROM address are configured. */
    public boolean isEnabled() {
        return mailSenderProvider.getIfAvailable() != null && !fromAddress.isBlank();
    }

    public boolean sendWelcome(String to, UUID reportId, Report report) {
        if (!isEnabled() || to == null || to.isBlank()) return false;

        String reportUrl = publicBaseUrl + "/r/" + reportId;
        String body = """
                Hi,

                Your Rinmukt Debt MRI is ready:
                %s

                Quick read of your numbers:
                  • Health score: %d / 100 (%s)
                  • Total debt: ₹%,.0f
                  • Recommended path: %s

                What to do this week:
                  1. Open the link above on your laptop.
                  2. Read the "Your action plan" section under the recommended path.
                  3. Make the first phone call (script is in the report).

                I'll send you a quick check-in in 7 days to see how the call went.

                — Rinmukt
                Honest debt-free path for Indians · rinmukt.vercel.app
                """.formatted(
                reportUrl,
                report.getHealthScore(),
                report.getHealthLabel(),
                report.getTotalDebt(),
                humanPath(report.getRecommendedPathId())
        );

        return send(to, "Your Debt MRI report is ready", body);
    }

    public boolean sendDay7FollowUp(String to, UUID reportId, Report report) {
        if (!isEnabled() || to == null || to.isBlank()) return false;

        String reportUrl = publicBaseUrl + "/r/" + reportId;
        String body = """
                Hi,

                It's been 7 days. How did the bank call go?

                If you haven't called yet — open your report and use the script
                under the recommended path. It's a 10-minute call:
                %s

                If you called and the bank pushed back — the report's "If they
                push back" section has the exact comeback for the most common
                stalls (rate too high, processing fee, "you don't qualify").

                If you've moved past that step — tick off step 1 in your report.
                Watching the progress bar fill up is the most motivating thing
                you'll do this month.

                Hit reply and tell me what happened. I read every reply.

                — Rinmukt
                """.formatted(reportUrl);

        return send(to, "Day 7: how did the bank call go?", body);
    }

    private boolean send(String to, String subject, String body) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) return false;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            sender.send(msg);
            log.info("Email sent to {}: {}", to, subject);
            return true;
        } catch (Exception e) {
            log.warn("Email send failed to {}: {}", to, e.getMessage());
            return false;
        }
    }

    private static String humanPath(String pathId) {
        if (pathId == null) return "Smart Path";
        return switch (pathId) {
            case "SMART_PATH" -> "Smart Path (CC→EMI + Avalanche)";
            case "AGGRESSIVE_SMART_PATH" -> "Aggressive Smart Path (with ₹20K income boost)";
            case "DIY_SETTLEMENT" -> "DIY Negotiated Settlement";
            case "FULL_SETTLEMENT" -> "Full Settlement (Freed-style)";
            case "STATUS_QUO" -> "Status Quo";
            default -> pathId;
        };
    }
}
