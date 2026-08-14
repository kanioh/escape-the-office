package com.example.escapetheoffice.roadmap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.escapetheoffice.roadmap.dto.RoadmapEventCreateRequest;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;

@ExtendWith(MockitoExtension.class)
class RoadmapEventServiceTest {

    // 同じ値を何度も書くと片方だけ直して食い違うため定数にする
    private static final long USER_ID = 1L;
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
}
