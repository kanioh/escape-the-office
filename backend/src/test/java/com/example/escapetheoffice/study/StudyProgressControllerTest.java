package com.example.escapetheoffice.study;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.study.dto.StudyProgressResponse;
import com.example.escapetheoffice.study.dto.StudyProgressUpdateRequest;

@WebMvcTest(StudyProgressController.class)
class StudyProgressControllerTest {

    private static final String VALID_REQUEST_BODY = """
            {
                "progressPercent": 55
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyProgressService studyProgressService;

    @Test
    @DisplayName("200 で学習項目ごとの進捗一覧を返す")
    void findAllReturnsOk() throws Exception {
        given(studyProgressService.findAll()).willReturn(List.of(
                new StudyProgressResponse(1L, "Spring Boot", StudyStatus.IN_PROGRESS, 40),
                new StudyProgressResponse(2L, "AWS", StudyStatus.NOT_STARTED, 0),
                new StudyProgressResponse(3L, "Docker", StudyStatus.DONE, 100)));

        mockMvc.perform(get("/api/study-progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                // 進捗が登録されている項目
                .andExpect(jsonPath("$[0].studyItemId").value(1))
                .andExpect(jsonPath("$[0].studyItemName").value("Spring Boot"))
                // enum は定数名の文字列として出力される
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].progressPercent").value(40))
                // 進捗が未登録の項目
                .andExpect(jsonPath("$[1].status").value("NOT_STARTED"))
                .andExpect(jsonPath("$[1].progressPercent").value(0));
    }

    @Test
    @DisplayName("更新は 200 で更新後の進捗を返す")
    void updateReturnsOk() throws Exception {
        // URL の studyItemId が Service へ渡ることを、eq(1L) で固定して確かめる
        given(studyProgressService.update(eq(1L), any(StudyProgressUpdateRequest.class)))
                .willReturn(new StudyProgressResponse(1L, "Spring Boot", StudyStatus.IN_PROGRESS, 55));

        mockMvc.perform(put("/api/study-progress/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyItemId").value(1))
                .andExpect(jsonPath("$.studyItemName").value("Spring Boot"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.progressPercent").value(55));
    }

    @Test
    @DisplayName("進捗率が 100 を超えたら 400 を返す")
    void updateReturnsBadRequestWhenPercentTooLarge() throws Exception {
        mockMvc.perform(put("/api/study-progress/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "progressPercent": 150
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("progressPercent"));
    }

    @Test
    @DisplayName("進捗率が無ければ 400 を返す")
    void updateReturnsBadRequestWhenPercentMissing() throws Exception {
        mockMvc.perform(put("/api/study-progress/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @Test
    @DisplayName("Service が対象なし例外を投げたら 404 に変換する")
    void updateReturnsNotFoundWhenStudyItemMissing() throws Exception {
        given(studyProgressService.update(eq(999L), any(StudyProgressUpdateRequest.class)))
                .willThrow(new ResourceNotFoundException("学習項目ID 999 は存在しません"));

        mockMvc.perform(put("/api/study-progress/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("学習項目ID 999 は存在しません"));
    }
}
