package com.example.escapetheoffice.expense;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotResponse;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotUpdateRequest;

@WebMvcTest(ExpenseSnapshotController.class)
class ExpenseSnapshotControllerTest {

    // @PastOrPresent に引っかからないよう過去の固定日を使う
    private static final LocalDate RECORDED_ON = LocalDate.of(2026, 8, 1);

    private static final String VALID_REQUEST_BODY = """
            {
                "monthlyExpense": 200000
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseSnapshotService expenseSnapshotService;

    @Test
    @DisplayName("正しいリクエストなら 200 で登録結果を返す")
    void updateReturnsOk() throws Exception {
        // URL の日付が LocalDate に変換されて Service へ渡ることも確かめる
        given(expenseSnapshotService.update(
                eq(RECORDED_ON), any(ExpenseSnapshotUpdateRequest.class)))
                .willReturn(new ExpenseSnapshotResponse(1L, RECORDED_ON, 200_000L));

        mockMvc.perform(put("/api/expenses/2026-08-01")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.recordedOn").value("2026-08-01"))
                .andExpect(jsonPath("$.monthlyExpense").value(200000));
    }

    @Test
    @DisplayName("必須項目が無ければ 400 と項目ごとのエラーを返す")
    void updateReturnsBadRequestWhenFieldsMissing() throws Exception {
        // recordedOn は URL へ移ったため、本文の必須項目は生活費のみ
        mockMvc.perform(put("/api/expenses/2026-08-01")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @Test
    @DisplayName("生活費が 0 なら 400 を返す")
    void updateReturnsBadRequestWhenExpenseIsZero() throws Exception {
        mockMvc.perform(put("/api/expenses/2026-08-01")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "monthlyExpense": 0
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("monthlyExpense"));
    }

    @Test
    @DisplayName("未来日を指定したら 400 と recordedOn のエラーを返す")
    void updateReturnsBadRequestWhenRecordedOnIsFuture() throws Exception {
        // 上の 400 とは経路が違い、GlobalExceptionHandler に追加した
        // 引数の検証用の受け口が働くことで errors が付く
        mockMvc.perform(put("/api/expenses/2099-01-01")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST_BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("recordedOn"));
    }

    @Test
    @DisplayName("一覧は 200 で配列を返す")
    void findAllReturnsOk() throws Exception {
        given(expenseSnapshotService.findAll())
                .willReturn(List.of(
                        new ExpenseSnapshotResponse(2L, RECORDED_ON, 200_000L),
                        new ExpenseSnapshotResponse(1L, RECORDED_ON.minusDays(1), 180_000L)));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].monthlyExpense").value(180000));
    }

    @Test
    @DisplayName("最新1件は 200 で単一オブジェクトを返す")
    void findLatestReturnsOk() throws Exception {
        given(expenseSnapshotService.findLatest())
                .willReturn(new ExpenseSnapshotResponse(2L, RECORDED_ON, 200_000L));

        mockMvc.perform(get("/api/expenses/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.monthlyExpense").value(200000));
    }

    @Test
    @DisplayName("記録が無ければ最新1件は 404 に変換する")
    void findLatestReturnsNotFoundWhenEmpty() throws Exception {
        given(expenseSnapshotService.findLatest())
                .willThrow(new ResourceNotFoundException("生活費の記録がまだありません"));

        mockMvc.perform(get("/api/expenses/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("生活費の記録がまだありません"));
    }
}
