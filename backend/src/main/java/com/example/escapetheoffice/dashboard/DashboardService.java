package com.example.escapetheoffice.dashboard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.dashboard.dto.DashboardResponse;
import com.example.escapetheoffice.roadmap.RoadmapEventService;
import com.example.escapetheoffice.simulation.SurvivalSimulationService;
import com.example.escapetheoffice.simulation.dto.SurvivalSimulationResponse;
import com.example.escapetheoffice.study.StudyProgressService;

/**
 * ダッシュボード（画面①）が必要とするデータを1回の呼び出しで返す。
 * 専用のテーブルを持たず、既存の Service を組み合わせるだけなので Repository は持たない。
 */
@Service
public class DashboardService {

    // 画面に並べる学習進捗の件数。全件は画面③で見るため、ここでは直近だけ拾う
    private static final int RECENT_STUDY_PROGRESS_LIMIT = 3;

    private final SurvivalSimulationService survivalSimulationService;
    private final StudyProgressService studyProgressService;
    private final RoadmapEventService roadmapEventService;

    public DashboardService(
            SurvivalSimulationService survivalSimulationService,
            StudyProgressService studyProgressService,
            RoadmapEventService roadmapEventService) {
        this.survivalSimulationService = survivalSimulationService;
        this.studyProgressService = studyProgressService;
        this.roadmapEventService = roadmapEventService;
    }

    /**
     * 3つの取得を1つのトランザクションにまとめ、一貫した断面を返す。
     * 資産か生活費が未登録なら simulate() の例外がそのまま上がり 404 になる。
     * どちらが足りないかはそのメッセージで分かるため、ここでは捕まえない。
     */
    @Transactional(readOnly = true)
    public DashboardResponse find() {
        SurvivalSimulationResponse simulation = survivalSimulationService.simulate();

        return new DashboardResponse(
                simulation.totalAssets(),
                simulation.survivableMonths(),
                studyProgressService.findRecentlyUpdated(RECENT_STUDY_PROGRESS_LIMIT),
                // これから始まる予定が無ければ null。DTO 側で null を許容している
                roadmapEventService.findNext().orElse(null));
    }
}
