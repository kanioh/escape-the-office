package com.example.escapetheoffice.asset;

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

import com.example.escapetheoffice.asset.dto.AssetSnapshotCreateRequest;
import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;
import com.example.escapetheoffice.common.exception.DuplicateResourceException;
import com.example.escapetheoffice.common.exception.ResourceNotFoundException;

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

    @Test
    @DisplayName("一覧は 200 で配列を返す")
    void findAllReturnsOk() throws Exception {
        given(assetSnapshotService.findAll())
                .willReturn(List.of(
                        new AssetSnapshotResponse(2L, RECORDED_ON, 1_000_000L, 500_000L),
                        new AssetSnapshotResponse(1L, RECORDED_ON.minusDays(1), 900_000L, 400_000L)));

        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].cashAmount").value(900000));
    }

    @Test
    @DisplayName("最新1件は 200 で単一オブジェクトを返す")
    void findLatestReturnsOk() throws Exception {
        given(assetSnapshotService.findLatest())
                .willReturn(new AssetSnapshotResponse(2L, RECORDED_ON, 1_000_000L, 500_000L));

        mockMvc.perform(get("/api/assets/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.cashAmount").value(1000000));
    }

    @Test
    @DisplayName("記録が無ければ最新1件は 404 に変換する")
    void findLatestReturnsNotFoundWhenEmpty() throws Exception {
        given(assetSnapshotService.findLatest())
                .willThrow(new ResourceNotFoundException("資産の記録がまだありません"));

        mockMvc.perform(get("/api/assets/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("資産の記録がまだありません"));
    }
}
