package com.example.escapetheoffice.roadmap;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapEventRepository extends JpaRepository<RoadmapEvent, Long> {

    // 時系列で見る以外の使い道が無いため、並び順まで含めて固定する。
    // idx_roadmap_events_user_start が (user_id, start_date) のため、
    // 絞り込みと並べ替えを1回で処理できる
    List<RoadmapEvent> findAllByUserIdOrderByStartDateAsc(Long userId);

    // JpaRepository の findById だと id さえ分かれば他人の予定を更新・削除できてしまう。
    // userId も条件に含め、他人の予定は「見つからない」＝404 として扱う
    Optional<RoadmapEvent> findByIdAndUserId(Long id, Long userId);
}
