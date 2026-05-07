package in.rinmukt.controller;

import in.rinmukt.persistence.MriPersistenceService;
import in.rinmukt.persistence.MriSessionEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Founder dashboard endpoints. All gated by a shared X-Admin-Token header
 * matched against rinmukt.admin.token (env: ADMIN_TOKEN). When the token
 * is unset, every admin call is rejected with 503.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final MriPersistenceService persistence;
    private final String adminToken;

    public AdminController(
            MriPersistenceService persistence,
            @Value("${rinmukt.admin.token:}") String adminToken
    ) {
        this.persistence = persistence;
        this.adminToken = adminToken;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        ResponseEntity<?> auth = check(token);
        if (auth != null) return auth;

        // Fetch a window of recent sessions (up to 1000) and derive aggregates
        // client-side. Free Supabase has plenty of headroom at V1 traffic.
        List<MriSessionEntity> recent = persistence.findRecent(1000);
        long total = persistence.countAll();

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime today = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        OffsetDateTime sevenDaysAgo = now.minusDays(7);

        long countToday = recent.stream().filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().isBefore(today)).count();
        long count7d = recent.stream().filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().isBefore(sevenDaysAgo)).count();
        long withEmail = recent.stream().filter(s -> s.getEmail() != null && !s.getEmail().isBlank()).count();
        long welcomeSent = recent.stream().filter(s -> s.getWelcomeSentAt() != null).count();
        long day7Sent = recent.stream().filter(s -> s.getDay7SentAt() != null).count();

        Map<String, Long> byPath = new HashMap<>();
        Map<String, Long> byHealthLabel = new HashMap<>();
        for (MriSessionEntity s : recent) {
            if (s.getRecommendedPath() != null) {
                byPath.merge(s.getRecommendedPath(), 1L, Long::sum);
            }
            if (s.getReportJson() != null && s.getReportJson().getHealthLabel() != null) {
                byHealthLabel.merge(s.getReportJson().getHealthLabel(), 1L, Long::sum);
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("total", total);
        payload.put("today", countToday);
        payload.put("last7Days", count7d);
        payload.put("withEmail", withEmail);
        payload.put("welcomeSent", welcomeSent);
        payload.put("day7Sent", day7Sent);
        payload.put("byPath", byPath);
        payload.put("byHealthLabel", byHealthLabel);
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> sessions(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        ResponseEntity<?> auth = check(token);
        if (auth != null) return auth;

        List<Map<String, Object>> rows = persistence.findRecent(50).stream()
                .map(this::summarise)
                .toList();
        return ResponseEntity.ok(Map.of("sessions", rows));
    }

    private Map<String, Object> summarise(MriSessionEntity s) {
        Map<String, Object> row = new HashMap<>();
        UUID id = s.getId();
        row.put("id", id == null ? null : id.toString());
        row.put("email", s.getEmail());
        row.put("createdAt", s.getCreatedAt() == null ? null : s.getCreatedAt().toString());
        row.put("healthScore", s.getHealthScore());
        row.put("recommendedPath", s.getRecommendedPath());
        row.put("welcomeSent", s.getWelcomeSentAt() != null);
        row.put("day7Sent", s.getDay7SentAt() != null);
        if (s.getReportJson() != null) {
            row.put("healthLabel", s.getReportJson().getHealthLabel());
            row.put("totalDebt", s.getReportJson().getTotalDebt());
        }
        return row;
    }

    /** Returns null when authorised; otherwise the response to short-circuit with. */
    private ResponseEntity<?> check(String token) {
        if (adminToken == null || adminToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Admin endpoints disabled (ADMIN_TOKEN not set)"));
        }
        if (token == null || !token.equals(adminToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid admin token"));
        }
        return null;
    }
}
