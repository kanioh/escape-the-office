package com.example.escapetheoffice.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.dashboard.dto.DashboardResponse;
import com.example.escapetheoffice.roadmap.RoadmapEventService;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;
import com.example.escapetheoffice.simulation.SurvivalSimulationService;
import com.example.escapetheoffice.simulation.dto.SurvivalSimulationResponse;
import com.example.escapetheoffice.study.StudyProgressService;
import com.example.escapetheoffice.study.StudyStatus;
import com.example.escapetheoffice.study.dto.StudyProgressResponse;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final LocalDate RECORDED_ON = LocalDate.of(2026, 8, 18);

    @Mock
    private SurvivalSimulationService survivalSimulationService;

    @Mock
    private StudyProgressService studyProgressService;

    @Mock
    private RoadmapEventService roadmapEventService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("3つの Service の結果をまとめて返す")
    void findCombinesEachSource() {
        // 準備。値をすべて変えておき、取り違えがあれば分かるようにする
        given(survivalSimulationService.simulate()).willReturn(new SurvivalSimulationResponse(
                1_570_000L,
                250_000L,
                6L,
                new SurvivalSimulationResponse.BasedOn(RECORDED_ON, RECORDED_ON)));
        given(studyProgressService.findAll()).willReturn(List.of(
                new StudyProgressResponse(1L, "Spring Boot", StudyStatus.IN_PROGRESS, 55),
                new StudyProgressResponse(2L, "AWS", StudyStatus.NOT_STARTED, 0)));
        given(roadmapEventService.findNext()).willReturn(Optional.of(
                new RoadmapEventResponse(3L, "有給消化",
                        LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))));

        // 実行
        DashboardResponse response = dashboardService.find();

        // 検証
        assertThat(response.totalAssets()).isEqualTo(1_570_000L);
        assertThat(response.survivableMonths()).isEqualTo(6L);
        assertThat(response.studyProgress()).hasSize(2);
        assertThat(response.studyProgress().get(0).studyItemName()).isEqualTo("Spring Boot");
        assertThat(response.nextEvent().title()).isEqualTo("有給消化");
    }

    @Test
    @DisplayName("これから始まる予定が無ければ nextEvent は null になる")
    void findReturnsNullNextEventWhenNoUpcomingEvent() {
        // 準備
        given(survivalSimulationService.simulate()).willReturn(new SurvivalSimulationResponse(
                1_570_000L,
                250_000L,
                6L,
                new SurvivalSimulationResponse.BasedOn(RECORDED_ON, RECORDED_ON)));
        given(studyProgressService.findAll()).willReturn(List.of());
        given(roadmapEventService.findNext()).willReturn(Optional.empty());

        // 実行
        DashboardResponse response = dashboardService.find();

        // 検証（Optional のまま返さず null に変換していること）
        assertThat(response.nextEvent()).isNull();
        assertThat(response.totalAssets()).isEqualTo(1_570_000L);
    }

    @Test
    @DisplayName("資産や生活費が未登録なら例外を捕まえずそのまま投げる")
    void findPropagatesExceptionWhenSimulationUnavailable() {
        // 準備
        given(survivalSimulationService.simulate())
                .willThrow(new ResourceNotFoundException("資産の記録がまだありません"));

        // 実行・検証。
        // 画面を落としたくないからと try-catch を足すと、404 にする判断が崩れる
        assertThatThrownBy(() -> dashboardService.find())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("資産の記録がまだありません");
    }
}
