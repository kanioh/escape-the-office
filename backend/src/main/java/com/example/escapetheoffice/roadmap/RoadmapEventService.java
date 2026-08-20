package com.example.escapetheoffice.roadmap;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventCreateRequest;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventUpdateRequest;

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

    /**
     * これから始まる予定のうち最も早いものを返す（ダッシュボードの「次の目標」）。
     * 予定が無い、あるいはすべて過去、という状態は正常なので空で返す。
     * 「今日」の決定は Repository ではなくここで行う。
     */
    @Transactional(readOnly = true)
    public Optional<RoadmapEventResponse> findNext() {
        return roadmapEventRepository
                .findFirstByUserIdAndStartDateGreaterThanEqualOrderByStartDateAsc(
                        CURRENT_USER_ID, LocalDate.now())
                .map(RoadmapEventResponse::from);
    }

    /**
     * 予定の内容を置き換える。
     * 取得した Entity は JPA の管理下にあるため、値を変えるだけで
     * トランザクション終了時に UPDATE が発行される（save() は不要）。
     */
    @Transactional
    public RoadmapEventResponse update(Long id, RoadmapEventUpdateRequest request) {
        RoadmapEvent roadmapEvent = findOwnedOrThrow(id);

        roadmapEvent.update(request.title(), request.startDate(), request.endDate());

        // DTO はメモリ上の値を写すだけなので、UPDATE の発行前に作って問題ない
        return RoadmapEventResponse.from(roadmapEvent);
    }

    /** 存在しない予定の削除は 404 にするため、deleteById に任せず先に取得する */
    @Transactional
    public void delete(Long id) {
        RoadmapEvent roadmapEvent = findOwnedOrThrow(id);

        // 取得済みの Entity を渡す。deleteById(id) だと JPA が内部でもう一度 SELECT する
        roadmapEventRepository.delete(roadmapEvent);
    }

    /**
     * 自分の予定を取得する。無ければ 404 に変換される例外を投げる。
     * userId を条件に含めるため、他人の予定は存在しないものとして扱われる。
     */
    private RoadmapEvent findOwnedOrThrow(Long id) {
        return roadmapEventRepository.findByIdAndUserId(id, CURRENT_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("予定ID " + id + " は存在しません"));
    }
}
