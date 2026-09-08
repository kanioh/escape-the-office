package com.example.escapetheoffice.expense;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotResponse;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotUpdateRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseSnapshotController {

    private final ExpenseSnapshotService expenseSnapshotService;

    public ExpenseSnapshotController(ExpenseSnapshotService expenseSnapshotService) {
        this.expenseSnapshotService = expenseSnapshotService;
    }

    /**
     * その日の生活費を登録・更新する（UPSERT）。
     * 日付がこの記録を特定する鍵のため URL に置く。新規と更新で結果は同じなので 200 に統一する。
     */
    @PutMapping("/{recordedOn}")
    public ExpenseSnapshotResponse update(
            // URL 上はただの文字列。ISO 形式（yyyy-MM-dd）として解釈するよう明示する。
            // 見直した日の記録なので未来日は受け付けない
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PastOrPresent
            LocalDate recordedOn,
            @Valid @RequestBody ExpenseSnapshotUpdateRequest request) {

        return expenseSnapshotService.update(recordedOn, request);
    }

    @GetMapping
    public List<ExpenseSnapshotResponse> findAll() {
        return expenseSnapshotService.findAll();
    }

    /**
     * 1件を返す API なので、記録が無ければ 404 とする。
     * Service は Optional を返すだけにして、404 にするかどうかはここで決める。
     */
    @GetMapping("/latest")
    public ExpenseSnapshotResponse findLatest() {
        return expenseSnapshotService.findLatest()
                .orElseThrow(() -> new ResourceNotFoundException("生活費の記録がまだありません"));
    }
}
