package com.example.escapetheoffice.simulation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.simulation.dto.SurvivalSimulationResponse;

@WebMvcTest(SurvivalSimulationController.class)
class SurvivalSimulationControllerTest {

    private static final LocalDate ASSETS_RECORDED_ON = LocalDate.of(2026, 8, 8);
    private static final LocalDate EXPENSE_RECORDED_ON = LocalDate.of(2026, 8, 9);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SurvivalSimulationService survivalSimulationService;

    @Test
    @DisplayName("200 で計算結果と計算の根拠を返す")
    void simulateReturnsOk() throws Exception {
        given(survivalSimulationService.simulate()).willReturn(
                new SurvivalSimulationResponse(
                        1_570_000L,
                        200_000L,
                        7L,
                        new SurvivalSimulationResponse.BasedOn(
                                ASSETS_RECORDED_ON,
                                EXPENSE_RECORDED_ON)));

        mockMvc.perform(get("/api/simulation/survival"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssets").value(1570000))
                .andExpect(jsonPath("$.monthlyExpense").value(200000))
                .andExpect(jsonPath("$.survivableMonths").value(7))
                // 入れ子のオブジェクトはドットで辿る
                .andExpect(jsonPath("$.basedOn.assetsRecordedOn").value("2026-08-08"))
                .andExpect(jsonPath("$.basedOn.expenseRecordedOn").value("2026-08-09"));
    }

    @Test
    @DisplayName("計算の元データが無ければ 404 に変換する")
    void simulateReturnsNotFoundWhenDataMissing() throws Exception {
        given(survivalSimulationService.simulate())
                .willThrow(new ResourceNotFoundException("資産の記録がまだありません"));

        mockMvc.perform(get("/api/simulation/survival"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("資産の記録がまだありません"));
    }
}
