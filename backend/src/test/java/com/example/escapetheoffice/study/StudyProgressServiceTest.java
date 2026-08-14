package com.example.escapetheoffice.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.study.dto.StudyProgressResponse;
import com.example.escapetheoffice.study.dto.StudyProgressUpdateRequest;

@ExtendWith(MockitoExtension.class)
class StudyProgressServiceTest {

    private static final long USER_ID = 1L;

    private static final StudyItem SPRING_BOOT = new StudyItem(1L, "Spring Boot");
    private static final StudyItem AWS = new StudyItem(2L, "AWS");
    private static final StudyItem DOCKER = new StudyItem(3L, "Docker");

    @Mock
    private StudyItemRepository studyItemRepository;

    @Mock
    private StudyProgressRepository studyProgressRepository;

    @InjectMocks
    private StudyProgressService studyProgressService;

    @Test
    @DisplayName("進捗が未登録でもマスタ全件を未着手 0% で返す")
    void findAllReturnsAllMasterItemsWhenNoProgress() {
        // 準備
        given(studyItemRepository.findAll(Sort.by(Sort.Direction.ASC, "id")))
                .willReturn(List.of(SPRING_BOOT, AWS, DOCKER));
        given(studyProgressRepository.findAllByUserId(USER_ID)).willReturn(List.of());

        // 実行
        List<StudyProgressResponse> responses = studyProgressService.findAll();

        // 検証
        assertThat(responses)
                .extracting(
                        StudyProgressResponse::studyItemId,
                        StudyProgressResponse::studyItemName,
                        StudyProgressResponse::status,
                        StudyProgressResponse::progressPercent)
                .containsExactly(
                        tuple(1L, "Spring Boot", StudyStatus.NOT_STARTED, 0),
                        tuple(2L, "AWS", StudyStatus.NOT_STARTED, 0),
                        tuple(3L, "Docker", StudyStatus.NOT_STARTED, 0));
    }

    @Test
    @DisplayName("登録済みの進捗を当てはめ、未登録は未着手 0% で埋めてマスタ順に返す")
    void findAllMergesProgressIntoMasterItems() {
        // 準備
        given(studyItemRepository.findAll(Sort.by(Sort.Direction.ASC, "id")))
                .willReturn(List.of(SPRING_BOOT, AWS, DOCKER));
        // 並び順がマスタ由来であることを示すため、進捗はわざとマスタと違う順で返す。
        // status は進捗率から導出されるため、100 は DONE、40 は IN_PROGRESS になる
        given(studyProgressRepository.findAllByUserId(USER_ID)).willReturn(List.of(
                new StudyProgress(USER_ID, DOCKER, 100),
                new StudyProgress(USER_ID, SPRING_BOOT, 40)));

        // 実行
        List<StudyProgressResponse> responses = studyProgressService.findAll();

        // 検証
        assertThat(responses)
                .extracting(
                        StudyProgressResponse::studyItemId,
                        StudyProgressResponse::studyItemName,
                        StudyProgressResponse::status,
                        StudyProgressResponse::progressPercent)
                .containsExactly(
                        tuple(1L, "Spring Boot", StudyStatus.IN_PROGRESS, 40),
                        tuple(2L, "AWS", StudyStatus.NOT_STARTED, 0),
                        tuple(3L, "Docker", StudyStatus.DONE, 100));
    }

    @Test
    @DisplayName("登録済みの進捗はダーティチェックに任せ、save を呼ばずに更新する")
    void updateChangesExistingProgressWithoutSave() {
        // 準備
        StudyProgress existing = new StudyProgress(USER_ID, SPRING_BOOT, 40);
        given(studyItemRepository.findById(1L)).willReturn(Optional.of(SPRING_BOOT));
        given(studyProgressRepository.findByUserIdAndStudyItemId(USER_ID, 1L))
                .willReturn(Optional.of(existing));

        // 実行
        StudyProgressResponse response =
                studyProgressService.update(1L, new StudyProgressUpdateRequest(55));

        // 検証
        assertThat(response.studyItemId()).isEqualTo(1L);
        assertThat(response.studyItemName()).isEqualTo("Spring Boot");
        assertThat(response.status()).isEqualTo(StudyStatus.IN_PROGRESS);
        assertThat(response.progressPercent()).isEqualTo(55);
        // Entity 自体が書き換わっていることを確認する
        assertThat(existing.getProgressPercent()).isEqualTo(55);
        // save を呼ばないのは意図した実装。呼ぶよう変えたらここで気付ける
        verify(studyProgressRepository, never()).save(any());
    }

    @Test
    @DisplayName("進捗が未登録の項目は新規作成して保存する")
    void updateCreatesProgressWhenNotRegistered() {
        // 準備
        given(studyItemRepository.findById(2L)).willReturn(Optional.of(AWS));
        given(studyProgressRepository.findByUserIdAndStudyItemId(USER_ID, 2L))
                .willReturn(Optional.empty());
        // 保存された Entity がそのまま返る、という実際のリポジトリの挙動を再現する
        given(studyProgressRepository.save(any(StudyProgress.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // 実行
        StudyProgressResponse response =
                studyProgressService.update(2L, new StudyProgressUpdateRequest(30));

        // 検証
        assertThat(response.studyItemId()).isEqualTo(2L);
        assertThat(response.status()).isEqualTo(StudyStatus.IN_PROGRESS);
        assertThat(response.progressPercent()).isEqualTo(30);
    }

    @Test
    @DisplayName("進捗率 0 は未着手、100 は完了として扱う")
    void updateResolvesStatusFromProgressPercent() {
        // 準備
        StudyProgress existing = new StudyProgress(USER_ID, DOCKER, 50);
        given(studyItemRepository.findById(3L)).willReturn(Optional.of(DOCKER));
        given(studyProgressRepository.findByUserIdAndStudyItemId(USER_ID, 3L))
                .willReturn(Optional.of(existing));

        // 実行・検証（境界値。同じ Entity を続けて更新する）
        assertThat(studyProgressService.update(3L, new StudyProgressUpdateRequest(100)).status())
                .isEqualTo(StudyStatus.DONE);
        assertThat(studyProgressService.update(3L, new StudyProgressUpdateRequest(0)).status())
                .isEqualTo(StudyStatus.NOT_STARTED);
        assertThat(studyProgressService.update(3L, new StudyProgressUpdateRequest(99)).status())
                .isEqualTo(StudyStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("存在しない学習項目を指定すると例外を投げ、進捗には触れない")
    void updateThrowsWhenStudyItemNotFound() {
        // 準備
        given(studyItemRepository.findById(999L)).willReturn(Optional.empty());

        // 実行・検証
        assertThatThrownBy(
                () -> studyProgressService.update(999L, new StudyProgressUpdateRequest(10)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        // 外部キー制約違反で 500 にならないよう、進捗テーブルへは一切アクセスしない
        verify(studyProgressRepository, never()).findByUserIdAndStudyItemId(any(), any());
        verify(studyProgressRepository, never()).save(any());
    }
}
