package com.example.escapetheoffice.dashboard;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.dashboard.dto.DashboardResponse;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;
import com.example.escapetheoffice.study.StudyStatus;
import com.example.escapetheoffice.study.dto.StudyProgressResponse;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("200 で画面に必要な4項目を返す")
    void findReturnsOk() throws Exception {
        given(dashboardService.find()).willReturn(new DashboardResponse(
                1_570_000L,
                6L,
                List.of(
                        new StudyProgressResponse(1L, "Spring Boot", StudyStatus.IN_PROGRESS, 55),
                        new StudyProgressResponse(2L, "AWS", StudyStatus.NOT_STARTED, 0)),
                new RoadmapEventResponse(3L, "有給消化",
                        LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAssets").value(1570000))
                .andExpect(jsonPath("$.survivableMonths").value(6))
                .andExpect(jsonPath("$.recentStudyProgress.length()").value(2))
                .andExpect(jsonPath("$.recentStudyProgress[0].studyItemName").value("Spring Boot"))
                // 入れ子はドットで辿る
                .andExpect(jsonPath("$.nextEvent.title").value("有給消化"))
                .andExpect(jsonPath("$.nextEvent.startDate").value("2026-09-01"));
    }

    @Test
    @DisplayName("資産や生活費が未登録なら 404 に変換する")
    void findReturnsNotFoundWhenSimulationUnavailable() throws Exception {
        given(dashboardService.find())
                .willThrow(new ResourceNotFoundException("資産の記録がまだありません"));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("資産の記録がまだありません"));
    }
}
