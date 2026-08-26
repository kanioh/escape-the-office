package com.example.escapetheoffice.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.example.escapetheoffice.common.exception.DuplicateResourceException;
import com.example.escapetheoffice.study.dto.StudyItemCreateRequest;
import com.example.escapetheoffice.study.dto.StudyItemResponse;

@ExtendWith(MockitoExtension.class)
class StudyItemServiceTest {

    private static final long ITEM_ID = 1L;
    private static final String ITEM_NAME = "Kubernetes";

    @Mock
    private StudyItemRepository studyItemRepository;

    @InjectMocks
    private StudyItemService studyItemService;

    @Test
    @DisplayName("学習項目を登録する")
    void createSavesStudyItem() {
        given(studyItemRepository.existsByName(ITEM_NAME)).willReturn(false);
        given(studyItemRepository.save(any(StudyItem.class)))
                .willReturn(new StudyItem(ITEM_ID, ITEM_NAME));

        StudyItemResponse response = studyItemService.create(new StudyItemCreateRequest(ITEM_NAME));

        assertThat(response.id()).isEqualTo(ITEM_ID);
        assertThat(response.name()).isEqualTo(ITEM_NAME);
    }

    @Test
    @DisplayName("前後の空白を除去して保存する")
    void createTrimsName() {
        given(studyItemRepository.existsByName(ITEM_NAME)).willReturn(false);
        given(studyItemRepository.save(any(StudyItem.class)))
                .willReturn(new StudyItem(ITEM_ID, ITEM_NAME));

        studyItemService.create(new StudyItemCreateRequest("  " + ITEM_NAME + "  "));

        // 空白を除去した名前で存在チェックと保存が行われることを確かめる
        verify(studyItemRepository).existsByName(ITEM_NAME);

        ArgumentCaptor<StudyItem> captor = ArgumentCaptor.forClass(StudyItem.class);
        verify(studyItemRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo(ITEM_NAME);
    }

    @Test
    @DisplayName("同名の項目が既にあれば重複例外を投げ、保存しない")
    void createThrowsWhenNameDuplicated() {
        given(studyItemRepository.existsByName(ITEM_NAME)).willReturn(true);

        assertThatThrownBy(() -> studyItemService.create(new StudyItemCreateRequest(ITEM_NAME)))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(ITEM_NAME);

        verify(studyItemRepository, never()).save(any(StudyItem.class));
    }

    @Test
    @DisplayName("学習項目を id 順で返す")
    void findAllReturnsItemsOrderedById() {
        given(studyItemRepository.findAll(Sort.by(Sort.Direction.ASC, "id")))
                .willReturn(List.of(
                        new StudyItem(1L, "Spring Boot"),
                        new StudyItem(2L, "AWS")));

        List<StudyItemResponse> responses = studyItemService.findAll();

        assertThat(responses).extracting(StudyItemResponse::name)
                .containsExactly("Spring Boot", "AWS");
    }
}
