package in.rinmukt.controller;

import in.rinmukt.domain.Report;
import in.rinmukt.dto.MriRequest;
import in.rinmukt.service.CalculationEngine;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MriController {

    private final CalculationEngine engine;

    public MriController(CalculationEngine engine) {
        this.engine = engine;
    }

    @PostMapping("/mri")
    public Report generate(@Valid @RequestBody MriRequest req) {
        return engine.run(req.toProfile());
    }
}
