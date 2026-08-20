package com.example.escapetheoffice.roadmap;

import java.time.LocalDate;
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

    /**
     * 指定日以降で最も早く始まる予定を1件返す（ダッシュボードの「次の目標」）。
     * GreaterThanEqual は >= の意味。引数はメソッド名に書いた条件の順に対応する。
     * 基準日を引数で受け取るのは、ここで LocalDate.now() を呼ぶと
     * テストで「今日」を固定できなくなるため。
     */
    Optional<RoadmapEvent> findFirstByUserIdAndStartDateGreaterThanEqualOrderByStartDateAsc(
            Long userId, LocalDate from);
}
