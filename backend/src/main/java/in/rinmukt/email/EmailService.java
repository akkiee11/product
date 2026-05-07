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
 * Each email goes out as both HTML (designed) and plain-text (fallback for
 * screen readers and clients with HTML disabled). No-ops silently when
 * BREVO_API_KEY or EMAIL_FROM are unset.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    // Brand
    private static final String COLOR_INK = "#0b1726";
    private static final String COLOR_ACCENT = "#ff6b35";
    private static final String COLOR_MONEY = "#0f9d58";
    private static final String COLOR_BG = "#f6f7f9";

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
        String pathName = humanPath(report.getRecommendedPathId());
        String label = report.getHealthLabel() == null ? "" : report.getHealthLabel();
        String labelColor = labelColor(label);
        String totalDebt = formatINR(report.getTotalDebt());

        String html = layout(
                "Your Debt MRI is ready",
                """
                <p style="margin:0 0 16px;font-size:15px;line-height:1.6;color:#1f2937;">Hi,</p>
                <p style="margin:0 0 24px;font-size:15px;line-height:1.6;color:#1f2937;">
                    We've analysed the numbers. Here's the headline of your report.
                </p>

                %s

                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="margin:24px 0;border-collapse:separate;">
                  <tr>
                    <td style="padding:14px 18px;background:%s;border-radius:10px;font-size:14px;line-height:1.6;color:#1f2937;">
                      <strong>Total debt:</strong> %s<br/>
                      <strong>Recommended path:</strong> %s
                    </td>
                  </tr>
                </table>

                %s

                <p style="margin:32px 0 8px;font-size:11px;letter-spacing:1.2px;text-transform:uppercase;color:#6b7280;font-weight:600;">
                    What to do this week
                </p>
                <ol style="margin:0 0 24px;padding-left:20px;font-size:15px;line-height:1.7;color:#1f2937;">
                    <li>Open the report on your laptop.</li>
                    <li>Read the <em>"Your action plan"</em> section under the recommended path.</li>
                    <li>Make the first phone call (script is in the report).</li>
                </ol>

                <p style="margin:24px 0 0;font-size:14px;line-height:1.6;color:#4b5563;">
                    I'll send you a quick check-in in 7 days to see how the call went.
                </p>
                <p style="margin:8px 0 0;font-size:14px;color:#4b5563;">— Rinmukt</p>
                """.formatted(
                        scoreCard(report.getHealthScore(), label, labelColor),
                        COLOR_BG,
                        totalDebt,
                        pathName,
                        cta(reportUrl, "Open my report →")
                )
        );

        String text = """
                Your Rinmukt Debt MRI is ready:
                %s

                Health score: %d / 100 (%s)
                Total debt: %s
                Recommended path: %s

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
                label,
                totalDebt,
                pathName
        );

        return send(to, "Your Debt MRI report is ready", html, text);
    }

    public boolean sendDay7FollowUp(String to, UUID reportId, Report report) {
        if (!isEnabled() || to == null || to.isBlank()) return false;
        String reportUrl = publicBaseUrl + "/r/" + reportId;

        String html = layout(
                "Day 7 — how did the bank call go?",
                """
                <p style="margin:0 0 16px;font-size:15px;line-height:1.6;color:#1f2937;">
                    It's been 7 days. How did the bank call go?
                </p>

                <p style="margin:16px 0;font-size:15px;line-height:1.7;color:#1f2937;">
                    <strong>If you haven't called yet</strong> — open your report and use
                    the script under the recommended path. It's a 10-minute call.
                </p>
                <p style="margin:16px 0;font-size:15px;line-height:1.7;color:#1f2937;">
                    <strong>If the bank pushed back</strong> — the report's
                    <em>"If they push back"</em> section has the exact comebacks for
                    rate-too-high, processing-fee, and "you don't qualify" stalls.
                </p>
                <p style="margin:16px 0 24px;font-size:15px;line-height:1.7;color:#1f2937;">
                    <strong>If you've moved past step 1</strong> — open the report and
                    tick it off. Watching the progress bar fill up is the most motivating
                    thing you'll do this month.
                </p>

                %s

                <p style="margin:28px 0 0;font-size:14px;line-height:1.6;color:#4b5563;">
                    Hit reply and tell me what happened. I read every reply.
                </p>
                <p style="margin:8px 0 0;font-size:14px;color:#4b5563;">— Rinmukt</p>
                """.formatted(cta(reportUrl, "Open my report →"))
        );

        String text = """
                It's been 7 days. How did the bank call go?

                If you haven't called yet — open your report and use the script
                under the recommended path. It's a 10-minute call:
                %s

                If the bank pushed back — the report's "If they push back" section
                has the exact comebacks (rate too high, processing fee, "you don't
                qualify").

                If you've moved past step 1 — tick it off in your report.

                Hit reply and tell me what happened. I read every reply.

                — Rinmukt
                """.formatted(reportUrl);

        return send(to, "Day 7: how did the bank call go?", html, text);
    }

    private boolean send(String to, String subject, String html, String text) {
        try {
            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", "Rinmukt", "email", fromAddress),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "htmlContent", html,
                    "textContent", text
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

    /* ---------- HTML building blocks ---------- */

    private String layout(String headline, String bodyHtml) {
        return """
                <!doctype html>
                <html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:%s;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;color:#0b1726;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:%s;padding:32px 12px;">
                    <tr><td align="center">
                      <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;width:100%%;background:#ffffff;border-radius:14px;overflow:hidden;box-shadow:0 1px 2px rgba(11,23,38,0.06);">
                        <tr>
                          <td style="background:%s;padding:18px 28px;color:#ffffff;">
                            <span style="font-size:18px;font-weight:700;letter-spacing:0.2px;color:%s;">Rinmukt</span>
                            <span style="color:#3a4a5e;margin:0 8px;">·</span>
                            <span style="font-size:18px;font-weight:600;color:#ffffff;">ऋणमुक्त</span>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:36px 32px 12px;">
                            <h1 style="margin:0 0 24px;font-size:24px;line-height:1.25;color:%s;font-weight:800;letter-spacing:-0.4px;">%s</h1>
                            %s
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:24px 32px 28px;border-top:1px solid #eef0f3;">
                            <p style="margin:0;font-size:12px;line-height:1.5;color:#9ca3af;">
                              Honest debt-free path for Indians · <a href="%s" style="color:#9ca3af;">rinmukt.vercel.app</a>
                            </p>
                            <p style="margin:6px 0 0;font-size:11px;line-height:1.5;color:#9ca3af;">
                              Information &amp; advisory tool. Not a registered debt counsellor or lender.
                            </p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body></html>
                """.formatted(COLOR_BG, COLOR_BG, COLOR_INK, COLOR_ACCENT, COLOR_INK, headline, bodyHtml, publicBaseUrl);
    }

    private String scoreCard(int score, String label, String labelColor) {
        return """
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="margin:8px 0 0;border-collapse:separate;">
                  <tr>
                    <td style="padding:18px 22px;background:%s;border-radius:12px;border:1px solid #e5e7eb;">
                      <div style="font-size:11px;letter-spacing:1.2px;text-transform:uppercase;color:#6b7280;font-weight:600;">Health score</div>
                      <div style="margin-top:4px;font-size:30px;font-weight:800;color:%s;letter-spacing:-0.5px;">
                        %d<span style="font-size:18px;color:#9ca3af;font-weight:600;"> / 100</span>
                      </div>
                      <div style="margin-top:8px;display:inline-block;padding:4px 10px;border-radius:999px;background:%s;color:#ffffff;font-size:11px;font-weight:700;letter-spacing:0.5px;">%s</div>
                    </td>
                  </tr>
                </table>
                """.formatted(COLOR_BG, COLOR_INK, score, labelColor, label);
    }

    private String cta(String url, String label) {
        return """
                <table role="presentation" cellpadding="0" cellspacing="0" style="margin:8px 0 0;border-collapse:separate;">
                  <tr><td style="background:%s;border-radius:10px;">
                    <a href="%s" style="display:inline-block;padding:14px 28px;font-size:15px;font-weight:600;color:#ffffff;text-decoration:none;letter-spacing:0.2px;">%s</a>
                  </td></tr>
                </table>
                """.formatted(COLOR_INK, url, label);
    }

    private static String labelColor(String label) {
        return switch (label) {
            case "HEALTHY" -> COLOR_MONEY;
            case "MANAGEABLE" -> "#d97706";
            case "RISKY" -> "#dc2626";
            case "CRITICAL" -> "#991b1b";
            default -> "#6b7280";
        };
    }

    private static String formatINR(double amount) {
        long rounded = Math.round(amount);
        String s = Long.toString(rounded);
        StringBuilder out = new StringBuilder();
        int n = s.length();
        if (n <= 3) {
            out.append(s);
        } else {
            out.append(s.substring(0, n - 3));
            // Insert commas every 2 digits before the last 3 (Indian format)
            int i = out.length() - 2;
            while (i > 0) {
                out.insert(i, ',');
                i -= 2;
            }
            out.append(',').append(s.substring(n - 3));
        }
        return "₹" + out;
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
