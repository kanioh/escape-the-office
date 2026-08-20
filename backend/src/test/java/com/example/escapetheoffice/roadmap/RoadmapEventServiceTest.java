package com.example.escapetheoffice.roadmap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventCreateRequest;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventUpdateRequest;

@ExtendWith(MockitoExtension.class)
class RoadmapEventServiceTest {

    // 同じ値を何度も書くと片方だけ直して食い違うため定数にする
    private static final long USER_ID = 1L;
    private static final long EVENT_ID = 1L;
    private static final String TITLE = "Spring Boot学習";
    private static final LocalDate START_DATE = LocalDate.of(2026, 10, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 12, 31);

    @Mock
    private RoadmapEventRepository roadmapEventRepository;

    @InjectMocks
    private RoadmapEventService roadmapEventService;

    @Test
    @DisplayName("予定を登録し、固定のユーザーIDを付けて保存する")
    void createSavesRoadmapEvent() {
        // 準備
        RoadmapEventCreateRequest request =
                new RoadmapEventCreateRequest(TITLE, START_DATE, END_DATE);
        given(roadmapEventRepository.save(any(RoadmapEvent.class)))
                .willReturn(new RoadmapEvent(USER_ID, TITLE, START_DATE, END_DATE));

        // 実行
        RoadmapEventResponse response = roadmapEventService.create(request);

        // 検証
        assertThat(response.title()).isEqualTo(TITLE);
        assertThat(response.startDate()).isEqualTo(START_DATE);
        assertThat(response.endDate()).isEqualTo(END_DATE);

        // userId はレスポンスに含まれず戻り値から確認できないため、
        // 保存時に渡した Entity を捕まえて中身を見る
        ArgumentCaptor<RoadmapEvent> captor = ArgumentCaptor.forClass(RoadmapEvent.class);
        verify(roadmapEventRepository).save(captor.capture());
        RoadmapEvent saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getTitle()).isEqualTo(TITLE);
        assertThat(saved.getStartDate()).isEqualTo(START_DATE);
        assertThat(saved.getEndDate()).isEqualTo(END_DATE);
    }

    @Test
    @DisplayName("終了日が未定でも登録できる")
    void createAcceptsNullEndDate() {
        // 準備
        RoadmapEventCreateRequest request =
                new RoadmapEventCreateRequest("フリーランス準備", START_DATE, null);
        given(roadmapEventRepository.save(any(RoadmapEvent.class)))
                .willReturn(new RoadmapEvent(USER_ID, "フリーランス準備", START_DATE, null));

        // 実行
        RoadmapEventResponse response = roadmapEventService.create(request);

        // 検証（null が既定値に置き換えられず、そのまま保持されること）
        assertThat(response.title()).isEqualTo("フリーランス準備");
        assertThat(response.endDate()).isNull();
    }

    @Test
    @DisplayName("一覧は予定を DTO に変換して返す")
    void findAllReturnsResponses() {
        // 準備（並び順は Repository が保証するため、返ってきた順のまま変換されることを見る）
        given(roadmapEventRepository.findAllByUserIdOrderByStartDateAsc(eq(USER_ID)))
                .willReturn(List.of(
                        new RoadmapEvent(USER_ID, "有給消化",
                                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)),
                        new RoadmapEvent(USER_ID, TITLE, START_DATE, END_DATE)));

        // 実行
        List<RoadmapEventResponse> responses = roadmapEventService.findAll();

        // 検証
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).title()).isEqualTo("有給消化");
        assertThat(responses.get(1).title()).isEqualTo(TITLE);
        assertThat(responses.get(1).endDate()).isEqualTo(END_DATE);
    }

    @Test
    @DisplayName("予定が1件も無ければ空リストを返す")
    void findAllReturnsEmptyListWhenNoEvents() {
        // 準備
        given(roadmapEventRepository.findAllByUserIdOrderByStartDateAsc(eq(USER_ID)))
                .willReturn(List.of());

        // 実行
        List<RoadmapEventResponse> responses = roadmapEventService.findAll();

        // 検証（一覧が空なのは正常。例外にしない）
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("次の目標は直近の予定を DTO に変換して返す")
    void findNextReturnsUpcomingEvent() {
        // 準備。基準日は Service 内の LocalDate.now() で決まるため any で受ける
        given(roadmapEventRepository
                .findFirstByUserIdAndStartDateGreaterThanEqualOrderByStartDateAsc(
                        eq(USER_ID), any(LocalDate.class)))
                .willReturn(Optional.of(
                        new RoadmapEvent(USER_ID, "有給消化",
                                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))));

        // 実行
        Optional<RoadmapEventResponse> response = roadmapEventService.findNext();

        // 検証
        assertThat(response).isPresent();
        assertThat(response.get().title()).isEqualTo("有給消化");
        assertThat(response.get().startDate()).isEqualTo(LocalDate.of(2026, 9, 1));
    }

    @Test
    @DisplayName("これから始まる予定が無ければ次の目標は空を返す")
    void findNextReturnsEmptyWhenNoUpcomingEvent() {
        // 準備
        given(roadmapEventRepository
                .findFirstByUserIdAndStartDateGreaterThanEqualOrderByStartDateAsc(
                        eq(USER_ID), any(LocalDate.class)))
                .willReturn(Optional.empty());

        // 実行・検証（予定が無い、すべて過去、はどちらも正常。例外にしない）
        assertThat(roadmapEventService.findNext()).isEmpty();
    }

    @Test
    @DisplayName("予定を更新する際はダーティチェックに任せ、save を呼ばない")
    void updateChangesEventWithoutSave() {
        // 準備
        RoadmapEvent existing = new RoadmapEvent(USER_ID, TITLE, START_DATE, END_DATE);
        given(roadmapEventRepository.findByIdAndUserId(EVENT_ID, USER_ID))
                .willReturn(Optional.of(existing));

        // 実行
        RoadmapEventResponse response = roadmapEventService.update(
                EVENT_ID,
                new RoadmapEventUpdateRequest("Spring Boot 復習", LocalDate.of(2026, 8, 20), null));

        // 検証
        assertThat(response.title()).isEqualTo("Spring Boot 復習");
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 8, 20));
        // 終了日ありから未定へ戻せること
        assertThat(response.endDate()).isNull();
        // Entity 自体が書き換わっていることを確認する
        assertThat(existing.getTitle()).isEqualTo("Spring Boot 復習");
        assertThat(existing.getEndDate()).isNull();
        // save を呼ばないのは意図した実装。呼ぶよう変えたらここで気付ける
        verify(roadmapEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("更新対象が無ければ例外を投げ、何も保存しない")
    void updateThrowsWhenNotFound() {
        // 準備（他人の予定もここでは空が返る）
        given(roadmapEventRepository.findByIdAndUserId(999L, USER_ID))
                .willReturn(Optional.empty());

        // 実行・検証
        assertThatThrownBy(() -> roadmapEventService.update(
                999L, new RoadmapEventUpdateRequest(TITLE, START_DATE, END_DATE)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(roadmapEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("予定を削除する際は取得済みの Entity を渡す")
    void deleteRemovesEvent() {
        // 準備
        RoadmapEvent existing = new RoadmapEvent(USER_ID, TITLE, START_DATE, END_DATE);
        given(roadmapEventRepository.findByIdAndUserId(EVENT_ID, USER_ID))
                .willReturn(Optional.of(existing));

        // 実行
        roadmapEventService.delete(EVENT_ID);

        // 検証（deleteById だと JPA が内部でもう一度 SELECT するため使わない）
        verify(roadmapEventRepository).delete(existing);
        verify(roadmapEventRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("削除対象が無ければ例外を投げ、何も削除しない")
    void deleteThrowsWhenNotFound() {
        // 準備
        given(roadmapEventRepository.findByIdAndUserId(999L, USER_ID))
                .willReturn(Optional.empty());

        // 実行・検証
        assertThatThrownBy(() -> roadmapEventService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        // 404 を返したつもりで実は消えていた、を防ぐ
        verify(roadmapEventRepository, never()).delete(any(RoadmapEvent.class));
        verify(roadmapEventRepository, never()).deleteById(any());
    }
}
