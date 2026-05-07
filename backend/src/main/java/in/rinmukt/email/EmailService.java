package in.rinmukt.email;

import in.rinmukt.domain.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sends transactional emails via Brevo's HTTP API (port 443 — never blocked
 * by Render free tier the way 587/2525 SMTP outbound is).
 *
 * No-ops silently when BREVO_API_KEY or EMAIL_FROM are unset, so dev
 * mode (and un-configured prod) still function. The MriController
 * never blocks on email — see EmailDispatcher's @Async dispatch.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    private final RestClient http;
    private final String apiKey;
    private final String fromAddress;
    private final String publicBaseUrl;

    public EmailService(
            @Value("${rinmukt.email.brevo-api-key:}") String apiKey,
            @Value("${rinmukt.email.from:}") String fromAddress,
            @Value("${rinmukt.email.public-base-url:https://rinmukt.vercel.app}") String publicBaseUrl
    ) {
        this.apiKey = apiKey;
        this.fromAddress = fromAddress;
        this.publicBaseUrl = publicBaseUrl;
        this.http = RestClient.builder()
                .baseUrl(BREVO_ENDPOINT)
                .defaultHeader("accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public boolean isEnabled() {
        return !apiKey.isBlank() && !fromAddress.isBlank();
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
        try {
            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", "Rinmukt", "email", fromAddress),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "textContent", body
            );
            String response = http.post()
                    .header("api-key", apiKey)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            log.info("Email sent via Brevo to {}: subject={} response={}", to, subject, response);
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
