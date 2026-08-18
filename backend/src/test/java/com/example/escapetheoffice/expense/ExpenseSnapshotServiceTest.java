package com.example.escapetheoffice.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotResponse;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotUpdateRequest;

@ExtendWith(MockitoExtension.class)
class ExpenseSnapshotServiceTest {

    private static final long USER_ID = 1L;
    private static final LocalDate RECORDED_ON = LocalDate.of(2026, 8, 9);
    private static final long MONTHLY_EXPENSE = 200_000L;

    @Mock
    private ExpenseSnapshotRepository expenseSnapshotRepository;

    @InjectMocks
    private ExpenseSnapshotService expenseSnapshotService;

    @Test
    @DisplayName("その日の記録が無ければ新規作成して保存する")
    void updateCreatesSnapshotWhenNotRegistered() {
        // 準備
        given(expenseSnapshotRepository.findByUserIdAndRecordedOn(USER_ID, RECORDED_ON))
                .willReturn(Optional.empty());
        // 保存された Entity がそのまま返る、という実際のリポジトリの挙動を再現する。
        // この台本が使われなければテストは失敗するため、save が呼ばれたことも保証される
        given(expenseSnapshotRepository.save(any(ExpenseSnapshot.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // 実行
        ExpenseSnapshotResponse response = expenseSnapshotService.update(
                RECORDED_ON, new ExpenseSnapshotUpdateRequest(MONTHLY_EXPENSE));

        // 検証
        assertThat(response.recordedOn()).isEqualTo(RECORDED_ON);
        assertThat(response.monthlyExpense()).isEqualTo(MONTHLY_EXPENSE);
    }

    @Test
    @DisplayName("その日の記録があればダーティチェックに任せ、save を呼ばずに上書きする")
    void updateOverwritesExistingSnapshotWithoutSave() {
        // 準備
        ExpenseSnapshot existing =
                new ExpenseSnapshot(USER_ID, RECORDED_ON, MONTHLY_EXPENSE);
        given(expenseSnapshotRepository.findByUserIdAndRecordedOn(USER_ID, RECORDED_ON))
                .willReturn(Optional.of(existing));

        // 実行
        ExpenseSnapshotResponse response = expenseSnapshotService.update(
                RECORDED_ON, new ExpenseSnapshotUpdateRequest(250_000L));

        // 検証
        assertThat(response.monthlyExpense()).isEqualTo(250_000L);
        // 日付は鍵なので変わらない
        assertThat(response.recordedOn()).isEqualTo(RECORDED_ON);
        // Entity 自体が書き換わっていることを確認する
        assertThat(existing.getMonthlyExpense()).isEqualTo(250_000L);
        // save を呼ばないのは意図した実装。呼ぶよう変えたらここで気付ける
        verify(expenseSnapshotRepository, never()).save(any());
    }

    @Test
    @DisplayName("一覧は記録を DTO に変換して返す")
    void findAllReturnsResponses() {
        // 準備
        given(expenseSnapshotRepository.findAllByUserIdOrderByRecordedOnDesc(eq(USER_ID)))
                .willReturn(List.of(
                        new ExpenseSnapshot(USER_ID, RECORDED_ON, MONTHLY_EXPENSE),
                        new ExpenseSnapshot(USER_ID, RECORDED_ON.minusDays(1), 180_000L)));

        // 実行
        List<ExpenseSnapshotResponse> responses = expenseSnapshotService.findAll();

        // 検証
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).recordedOn()).isEqualTo(RECORDED_ON);
        assertThat(responses.get(1).monthlyExpense()).isEqualTo(180_000L);
    }

    @Test
    @DisplayName("記録があれば最新1件を返す")
    void findLatestReturnsResponse() {
        // 準備
        given(expenseSnapshotRepository.findFirstByUserIdOrderByRecordedOnDesc(eq(USER_ID)))
                .willReturn(Optional.of(
                        new ExpenseSnapshot(USER_ID, RECORDED_ON, MONTHLY_EXPENSE)));

        // 実行
        ExpenseSnapshotResponse response = expenseSnapshotService.findLatest();

        // 検証
        assertThat(response.recordedOn()).isEqualTo(RECORDED_ON);
        assertThat(response.monthlyExpense()).isEqualTo(MONTHLY_EXPENSE);
    }

    @Test
    @DisplayName("記録が1件も無ければ例外を投げる")
    void findLatestThrowsWhenEmpty() {
        // 準備
        given(expenseSnapshotRepository.findFirstByUserIdOrderByRecordedOnDesc(eq(USER_ID)))
                .willReturn(Optional.empty());

        // 実行・検証
        assertThatThrownBy(() -> expenseSnapshotService.findLatest())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("生活費の記録がまだありません");
    }
}
