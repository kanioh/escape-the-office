package com.example.escapetheoffice.asset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.escapetheoffice.asset.dto.AssetSnapshotCreateRequest;
import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;
import com.example.escapetheoffice.common.exception.DuplicateResourceException;

@WebMvcTest(AssetSnapshotController.class)
class AssetSnapshotControllerTest {

    // @PastOrPresent に引っかからないよう過去の固定日を使う
    private static final LocalDate RECORDED_ON = LocalDate.of(2026, 8, 1);

    private static final String VALID_REQUEST_BODY = """
            {
              "recordedOn": "2026-08-01",
              "cashAmount": 1000000,
              "nisaAmount": 500000
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssetSnapshotService assetSnapshotService;

    @Test
    @DisplayName("正しいリクエストなら 201 で登録結果を返す")
    void createReturnsCreated() throws Exception {
        given(assetSnapshotService.create(any(AssetSnapshotCreateRequest.class)))
                .willReturn(new AssetSnapshotResponse(1L, RECORDED_ON, 1_000_000L, 500_000L));

        mockMvc.perform(post("/api/assets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.recordedOn").value("2026-08-01"))
                .andExpect(jsonPath("$.cashAmount").value(1000000))
                .andExpect(jsonPath("$.nisaAmount").value(500000));
    }

    @Test
    @DisplayName("必須項目が無ければ 400 と項目ごとのエラーを返す")
    void createReturnsBadRequestWhenFieldsMissing() throws Exception {
        mockMvc.perform(post("/api/assets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    @DisplayName("Service が重複例外を投げたら 409 に変換する")
    void createReturnsConflictWhenDuplicated() throws Exception {
        given(assetSnapshotService.create(any(AssetSnapshotCreateRequest.class)))
                .willThrow(new DuplicateResourceException("2026-08-01 の資産は既に登録されています"));

        mockMvc.perform(post("/api/assets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("2026-08-01 の資産は既に登録されています"));
    }
}
