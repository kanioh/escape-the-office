package com.example.escapetheoffice.simulation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.simulation.dto.SurvivalSimulationResponse;

@RestController
@RequestMapping("/api/simulation")
public class SurvivalSimulationController {

    private final SurvivalSimulationService survivalSimulationService;

    public SurvivalSimulationController(SurvivalSimulationService survivalSimulationService) {
        this.survivalSimulationService = survivalSimulationService;
    }

    /**
     * 計算結果そのものを返す API なので、計算できないなら 404 とする。
     * どちらが欠けているかは /api/assets/latest と /api/expenses/latest で分かる。
     * Controller はトランザクションの外側なので、ここで例外を投げても巻き戻しは起きない。
     */
    @GetMapping("/survival")
    public SurvivalSimulationResponse simulate() {
        return survivalSimulationService.simulate()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "資産または生活費の記録がまだありません"));
    }
}
