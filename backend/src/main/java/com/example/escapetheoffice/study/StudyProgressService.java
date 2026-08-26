package com.example.escapetheoffice.study;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.study.dto.StudyProgressResponse;
import com.example.escapetheoffice.study.dto.StudyProgressUpdateRequest;

@Service
public class StudyProgressService {

    // MVP は認証を持たないため固定。認証導入時はログインユーザーから取得する処理に置き換える
    private static final long CURRENT_USER_ID = 1L;

    private final StudyItemRepository studyItemRepository;
    private final StudyProgressRepository studyProgressRepository;

    public StudyProgressService(
            StudyItemRepository studyItemRepository,
            StudyProgressRepository studyProgressRepository) {
        this.studyItemRepository = studyItemRepository;
        this.studyProgressRepository = studyProgressRepository;
    }

    /**
     * 学習項目マスタを骨格として、進捗を当てはめて返す。
     * 進捗が未登録の項目も未着手 0% として含めるため、常にマスタの件数分が返る。
     * マスタを先に読むことで、進捗から関連を辿る必要が無くなり N+1 も避けられる。
     */
    @Transactional(readOnly = true)
    public List<StudyProgressResponse> findAll() {
        List<StudyItem> studyItems = studyItemRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

        // 項目ごとにリストを走査せずに済むよう、学習項目ID をキーにした表にしておく
        Map<Long, StudyProgress> progressByItemId =
                studyProgressRepository.findAllByUserId(CURRENT_USER_ID).stream()
                        .collect(Collectors.toMap(
                                progress -> progress.getStudyItem().getId(),
                                progress -> progress));

        return studyItems.stream()
                .map(item -> toResponse(item, progressByItemId.get(item.getId())))
                .toList();
    }

    /**
     * 最近更新した進捗を新しい順に返す（ダッシュボード用）。
     * 一度も進捗を登録していない項目は updated_at を持たないため、ここには現れない。
     * 「最近触ったもの」を並べる用途なので、未着手の項目が出てこないのは意図どおり。
     */
    @Transactional(readOnly = true)
    public List<StudyProgressResponse> findRecentlyUpdated(int limit) {
        return studyProgressRepository
                .findAllByUserIdOrderByUpdatedAtDesc(CURRENT_USER_ID, Limit.of(limit)).stream()
                .map(progress -> toResponse(progress.getStudyItem(), progress))
                .toList();
    }

    /**
     * 進捗率を更新する。進捗が未登録の項目にも使えるよう、無ければ作成する（UPSERT）。
     * 一覧が未登録の項目も返す以上、画面からは登録済みかどうかが分からないため、
     * 呼び出し側に登録の有無を意識させない。
     */
    @Transactional
    public StudyProgressResponse update(Long studyItemId, StudyProgressUpdateRequest request) {
        // 存在しない項目のまま新規作成に進むと外部キー制約違反で 500 になるため、先に弾く
        StudyItem studyItem = studyItemRepository.findById(studyItemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "学習項目ID " + studyItemId + " は存在しません"));

        Optional<StudyProgress> existing =
                studyProgressRepository.findByUserIdAndStudyItemId(CURRENT_USER_ID, studyItemId);

        StudyProgress progress;
        if (existing.isPresent()) {
            progress = existing.get();
            // 取得済みの Entity は JPA が追跡しているため、値を変えるだけで
            // トランザクション終了時に UPDATE が発行される（ダーティチェック）
            progress.changeProgress(request.progressPercent());
        } else {
            // new しただけの Entity は JPA の管理外なので、save() で管理下に入れる
            progress = studyProgressRepository.save(
                    new StudyProgress(CURRENT_USER_ID, studyItem, request.progressPercent()));
        }

        return toResponse(studyItem, progress);
    }

    /** 進捗が無い項目は未着手の 0% とみなす。この判断があるため DTO 側には置かない */
    private StudyProgressResponse toResponse(StudyItem item, StudyProgress progress) {
        if (progress == null) {
            return new StudyProgressResponse(
                    item.getId(), item.getName(), StudyStatus.NOT_STARTED, 0);
        }
        return new StudyProgressResponse(
                item.getId(), item.getName(), progress.getStatus(), progress.getProgressPercent());
    }
}
