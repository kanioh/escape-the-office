package com.example.escapetheoffice.study;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.example.escapetheoffice.common.exception.DuplicateResourceException;
import com.example.escapetheoffice.study.dto.StudyItemCreateRequest;
import com.example.escapetheoffice.study.dto.StudyItemResponse;

@WebMvcTest(StudyItemController.class)
class StudyItemControllerTest {

    private static final String VALID_REQUEST_BODY = """
            {
                "name": "Kubernetes"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyItemService studyItemService;

    @Test
    @DisplayName("登録は 201 で登録後の項目を返す")
    void createReturnsCreated() throws Exception {
        given(studyItemService.create(any(StudyItemCreateRequest.class)))
                .willReturn(new StudyItemResponse(6L, "Kubernetes"));

        mockMvc.perform(post("/api/study-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.name").value("Kubernetes"));
    }

    @Test
    @DisplayName("項目名が空白だけなら 400 を返す")
    void createReturnsBadRequestWhenNameBlank() throws Exception {
        mockMvc.perform(post("/api/study-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "   "
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @Test
    @DisplayName("Service が重複例外を投げたら 409 に変換する")
    void createReturnsConflictWhenNameDuplicated() throws Exception {
        given(studyItemService.create(any(StudyItemCreateRequest.class)))
                .willThrow(new DuplicateResourceException("学習項目「Kubernetes」は既に登録されています"));

        mockMvc.perform(post("/api/study-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("学習項目「Kubernetes」は既に登録されています"));
    }

    @Test
    @DisplayName("200 で学習項目の一覧を返す")
    void findAllReturnsOk() throws Exception {
        given(studyItemService.findAll()).willReturn(List.of(
                new StudyItemResponse(1L, "Spring Boot"),
                new StudyItemResponse(2L, "AWS")));

        mockMvc.perform(get("/api/study-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Spring Boot"));
    }
}
