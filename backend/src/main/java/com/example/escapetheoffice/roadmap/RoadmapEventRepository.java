package com.example.escapetheoffice.roadmap;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapEventRepository extends JpaRepository<RoadmapEvent, Long> {

    // 時系列で見る以外の使い道が無いため、並び順まで含めて固定する。
    // idx_roadmap_events_user_start が (user_id, start_date) のため、
    // 絞り込みと並べ替えを1回で処理できる
    List<RoadmapEvent> findAllByUserIdOrderByStartDateAsc(Long userId);
}
