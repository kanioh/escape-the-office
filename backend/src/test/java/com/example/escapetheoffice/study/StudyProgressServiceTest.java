package com.example.escapetheoffice.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.example.escapetheoffice.study.dto.StudyProgressResponse;

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
        // 並び順がマスタ由来であることを示すため、進捗はわざとマスタと違う順で返す
        given(studyProgressRepository.findAllByUserId(USER_ID)).willReturn(List.of(
                new StudyProgress(USER_ID, DOCKER, StudyStatus.DONE, 100),
                new StudyProgress(USER_ID, SPRING_BOOT, StudyStatus.IN_PROGRESS, 40)));

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
}
