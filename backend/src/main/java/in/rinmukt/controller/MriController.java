package in.rinmukt.controller;

import in.rinmukt.domain.Report;
import in.rinmukt.dto.MriRequest;
import in.rinmukt.persistence.MriPersistenceService;
import in.rinmukt.service.CalculationEngine;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MriController {

    private final CalculationEngine engine;
    private final MriPersistenceService persistence;

    public MriController(CalculationEngine engine, MriPersistenceService persistence) {
        this.engine = engine;
        this.persistence = persistence;
    }

    @PostMapping("/mri")
    public Report generate(@Valid @RequestBody MriRequest req) {
        Report report = engine.run(req.toProfile());
        persistence.save(req, report).ifPresent(report::setId);
        return report;
    }

    @GetMapping("/mri/{id}")
    public ResponseEntity<Report> get(@PathVariable UUID id) {
        return persistence.findReport(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
