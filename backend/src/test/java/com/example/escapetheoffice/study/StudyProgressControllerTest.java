package com.example.escapetheoffice.study;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.escapetheoffice.study.dto.StudyProgressResponse;

@WebMvcTest(StudyProgressController.class)
class StudyProgressControllerTest {

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
}
