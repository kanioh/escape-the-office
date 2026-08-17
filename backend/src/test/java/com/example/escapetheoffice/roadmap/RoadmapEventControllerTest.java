package com.example.escapetheoffice.roadmap;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.example.escapetheoffice.roadmap.dto.RoadmapEventCreateRequest;
import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;

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
}
