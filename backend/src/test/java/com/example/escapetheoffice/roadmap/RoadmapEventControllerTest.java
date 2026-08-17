package com.example.escapetheoffice.roadmap;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventCreateRequest;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventUpdateRequest;

@WebMvcTest(RoadmapEventController.class)
class RoadmapEventControllerTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 10, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 12, 31);

    private static final String VALID_REQUEST_BODY = """
            {
                "title": "Spring Boot学習",
                "startDate": "2026-10-01",
                "endDate": "2026-12-31"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoadmapEventService roadmapEventService;

    @Test
    @DisplayName("正しいリクエストなら 201 で登録結果を返す")
    void createReturnsCreated() throws Exception {
        given(roadmapEventService.create(any(RoadmapEventCreateRequest.class)))
                .willReturn(new RoadmapEventResponse(1L, "Spring Boot学習", START_DATE, END_DATE));

        mockMvc.perform(post("/api/roadmap-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Spring Boot学習"))
                .andExpect(jsonPath("$.startDate").value("2026-10-01"))
                .andExpect(jsonPath("$.endDate").value("2026-12-31"));
    }

    @Test
    @DisplayName("終了日を省略しても 201 で登録でき、endDate は null で返る")
    void createWithoutEndDateReturnsCreated() throws Exception {
        given(roadmapEventService.create(any(RoadmapEventCreateRequest.class)))
                .willReturn(new RoadmapEventResponse(2L, "フリーランス準備", START_DATE, null));

        mockMvc.perform(post("/api/roadmap-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "フリーランス準備",
                            "startDate": "2026-10-01"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("フリーランス準備"))
                // 項目が消えるのではなく null として出力される
                .andExpect(jsonPath("$.endDate").value(nullValue()));
    }

    @Test
    @DisplayName("タイトルが空白だけなら 400 を返す")
    void createReturnsBadRequestWhenTitleBlank() throws Exception {
        // @Valid で弾かれ Service には到達しないため、台本は書かない
        mockMvc.perform(post("/api/roadmap-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "   ",
                            "startDate": "2026-10-01"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    @DisplayName("終了日が開始日より前なら 400 を返す")
    void createReturnsBadRequestWhenEndDateIsBeforeStartDate() throws Exception {
        // 判定そのものは DateRangeValidatorTest で確認済み。
        // ここでは @ValidDateRange が DTO に付いていて @Valid から呼ばれることを確かめる
        mockMvc.perform(post("/api/roadmap-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "逆転した予定",
                            "startDate": "2026-12-31",
                            "endDate": "2026-10-01"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("endDate"))
                .andExpect(jsonPath("$.errors[0].message").value("終了日は開始日以降にしてください"));
    }

    @Test
    @DisplayName("一覧は 200 で配列を返す")
    void findAllReturnsOk() throws Exception {
        given(roadmapEventService.findAll()).willReturn(List.of(
                new RoadmapEventResponse(2L, "有給消化",
                        LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)),
                new RoadmapEventResponse(1L, "Spring Boot学習", START_DATE, END_DATE)));

        mockMvc.perform(get("/api/roadmap-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("有給消化"))
                .andExpect(jsonPath("$[1].title").value("Spring Boot学習"))
                .andExpect(jsonPath("$[1].startDate").value("2026-10-01"));
    }

    @Test
    @DisplayName("更新は 200 で更新後の内容を返す")
    void updateReturnsOk() throws Exception {
        // URL の id が Service へ渡ることを、eq(1L) で固定して確かめる
        given(roadmapEventService.update(eq(1L), any(RoadmapEventUpdateRequest.class)))
                .willReturn(new RoadmapEventResponse(
                        1L, "Spring Boot 復習", LocalDate.of(2026, 8, 20), END_DATE));

        mockMvc.perform(put("/api/roadmap-events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Spring Boot 復習",
                            "startDate": "2026-08-20",
                            "endDate": "2026-12-31"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Spring Boot 復習"))
                .andExpect(jsonPath("$.startDate").value("2026-08-20"));
    }

    @Test
    @DisplayName("更新でも期間が逆転していれば 400 を返す")
    void updateReturnsBadRequestWhenEndDateIsBeforeStartDate() throws Exception {
        // 更新用 DTO でも @ValidDateRange が効いていることを確かめる
        mockMvc.perform(put("/api/roadmap-events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "逆転した予定",
                            "startDate": "2026-12-31",
                            "endDate": "2026-10-01"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("endDate"));
    }

    @Test
    @DisplayName("削除は 204 で本文を返さない")
    void deleteReturnsNoContent() throws Exception {
        // モックの void メソッドは既定で何もしないため、台本は不要
        mockMvc.perform(delete("/api/roadmap-events/1"))
                .andExpect(status().isNoContent())
                // 204 は本文なしが意味なので、空であることまで確かめる
                .andExpect(content().string(""));

        verify(roadmapEventService).delete(1L);
    }

    @Test
    @DisplayName("削除対象が無ければ 404 に変換する")
    void deleteReturnsNotFound() throws Exception {
        // 戻り値が void のメソッドは given(...) に置けないため、willThrow から書く
        willThrow(new ResourceNotFoundException("予定ID 999 は存在しません"))
                .given(roadmapEventService).delete(999L);

        mockMvc.perform(delete("/api/roadmap-events/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("予定ID 999 は存在しません"));
    }
}
