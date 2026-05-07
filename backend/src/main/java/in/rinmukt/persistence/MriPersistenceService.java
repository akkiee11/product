package in.rinmukt.persistence;

import in.rinmukt.domain.Report;
import in.rinmukt.dto.MriRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

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
}
