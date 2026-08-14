package com.example.escapetheoffice.roadmap;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.roadmap.dto.RoadmapEventCreateRequest;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;

@Service
public class RoadmapEventService {

    // MVP は認証を持たないため固定。認証導入時はログインユーザーから取得する処理に置き換える
    private static final long CURRENT_USER_ID = 1L;

    private final RoadmapEventRepository roadmapEventRepository;

    public RoadmapEventService(RoadmapEventRepository roadmapEventRepository) {
        this.roadmapEventRepository = roadmapEventRepository;
    }

    /**
     * 予定を登録する。
     * 資産や生活費と違い、同じ期間に複数の予定が並ぶのは正常（学習しながら転職活動する等）なので
     * 重複チェックは行わない。テーブル側にもユニーク制約を置いていない。
     */
    @Transactional
    public RoadmapEventResponse create(RoadmapEventCreateRequest request) {
        RoadmapEvent roadmapEvent = new RoadmapEvent(
                CURRENT_USER_ID,
                request.title(),
                request.startDate(),
                request.endDate());

        // save() の戻り値には採番された id が入っているため、引数ではなく戻り値を使う
        RoadmapEvent saved = roadmapEventRepository.save(roadmapEvent);

        return RoadmapEventResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<RoadmapEventResponse> findAll() {
        return roadmapEventRepository.findAllByUserIdOrderByStartDateAsc(CURRENT_USER_ID).stream()
                .map(RoadmapEventResponse::from)
                .toList();
    }
}
