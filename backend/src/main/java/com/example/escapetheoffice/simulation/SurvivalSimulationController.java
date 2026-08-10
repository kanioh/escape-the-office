package com.example.escapetheoffice.simulation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.simulation.dto.SurvivalSimulationResponse;

@RestController
@RequestMapping("/api/simulation")
public class SurvivalSimulationController {

    private final SurvivalSimulationService survivalSimulationService;

    public SurvivalSimulationController(SurvivalSimulationService survivalSimulationService) {
        this.survivalSimulationService = survivalSimulationService;
    }

    @GetMapping("/survival")
    public SurvivalSimulationResponse simulate() {
        return survivalSimulationService.simulate();
    }
}
