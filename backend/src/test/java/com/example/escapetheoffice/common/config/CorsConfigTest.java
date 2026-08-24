package com.example.escapetheoffice.common.config;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.escapetheoffice.study.StudyProgressController;
import com.example.escapetheoffice.study.StudyProgressService;

// CORS 設定は特定の画面に紐づかないので、代表として学習進捗の API で検証する
@WebMvcTest(StudyProgressController.class)
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudyProgressService studyProgressService;

    @Test
    @DisplayName("許可オリジンからの PUT のプリフライトを許可する")
    void allowsPreflightFromAllowedOrigin() throws Exception {
        mockMvc.perform(options("/api/study-progress/1")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("PUT")));
    }

    @Test
    @DisplayName("許可していないオリジンからのプリフライトは 403 で拒否する")
    void rejectsPreflightFromUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/study-progress/1")
                .header(HttpHeaders.ORIGIN, "http://evil.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT"))
                .andExpect(status().isForbidden());
    }

    // GET は単純リクエストでプリフライトが発生しないため、本番レスポンス側のヘッダーを別途確かめる
    @Test
    @DisplayName("通常の GET にも許可ヘッダーを付けて返す")
    void addsAllowOriginHeaderToActualRequest() throws Exception {
        given(studyProgressService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/study-progress")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
    }
}
