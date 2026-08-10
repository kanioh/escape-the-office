package com.example.escapetheoffice.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.escapetheoffice.asset.AssetSnapshotService;
import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;
import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.expense.ExpenseSnapshotService;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotResponse;
import com.example.escapetheoffice.simulation.dto.SurvivalSimulationResponse;

@ExtendWith(MockitoExtension.class)
class SurvivalSimulationServiceTest {

    private static final LocalDate ASSETS_RECORDED_ON = LocalDate.of(2026, 8, 8);
    private static final LocalDate EXPENSE_RECORDED_ON = LocalDate.of(2026, 8, 9);

    @Mock
    private AssetSnapshotService assetSnapshotService;

    @Mock
    private ExpenseSnapshotService expenseSnapshotService;

    @InjectMocks
    private SurvivalSimulationService survivalSimulationService;

    @ParameterizedTest(name = "現金{0} + NISA{1} ÷ 生活費{2} = {3}ヶ月")
    @CsvSource({
            // 端数は切り捨てる（1,570,000 / 200,000 = 7.85）
            "1050000, 520000, 200000, 7",
            // 割り切れる場合
            "1000000,      0, 200000, 5",
            // 1ヶ月も持たない場合は 0 になる（150,000 / 200,000 = 0.75）
            "  50000, 100000, 200000, 0",
            // 資産ゼロ
            "      0,      0, 200000, 0",
    })
    @DisplayName("資産合計を生活費で割り、端数を切り捨てた月数を返す")
    void simulateCalculatesSurvivableMonths(
            long cashAmount, long nisaAmount, long monthlyExpense, long expectedMonths) {
        // 準備
        given(assetSnapshotService.findLatest()).willReturn(
                new AssetSnapshotResponse(1L, ASSETS_RECORDED_ON, cashAmount, nisaAmount));
        given(expenseSnapshotService.findLatest()).willReturn(
                new ExpenseSnapshotResponse(1L, EXPENSE_RECORDED_ON, monthlyExpense));

        // 実行
        SurvivalSimulationResponse response = survivalSimulationService.simulate();

        // 検証
        assertThat(response.totalAssets()).isEqualTo(cashAmount + nisaAmount);
        assertThat(response.monthlyExpense()).isEqualTo(monthlyExpense);
        assertThat(response.survivableMonths()).isEqualTo(expectedMonths);
    }

    @Test
    @DisplayName("計算に使ったデータの記録日を返す")
    void simulateReturnsBasedOnDates() {
        // 準備
        given(assetSnapshotService.findLatest()).willReturn(
                new AssetSnapshotResponse(1L, ASSETS_RECORDED_ON, 1_050_000L, 520_000L));
        given(expenseSnapshotService.findLatest()).willReturn(
                new ExpenseSnapshotResponse(1L, EXPENSE_RECORDED_ON, 200_000L));

        // 実行
        SurvivalSimulationResponse response = survivalSimulationService.simulate();

        // 検証
        assertThat(response.basedOn().assetsRecordedOn()).isEqualTo(ASSETS_RECORDED_ON);
        assertThat(response.basedOn().expenseRecordedOn()).isEqualTo(EXPENSE_RECORDED_ON);
    }

    @Test
    @DisplayName("資産の記録が無ければ例外がそのまま伝わり、生活費は取得しない")
    void simulateThrowsWhenAssetNotFound() {
        // 準備
        given(assetSnapshotService.findLatest())
                .willThrow(new ResourceNotFoundException("資産の記録がまだありません"));

        // 実行・検証
        assertThatThrownBy(() -> survivalSimulationService.simulate())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("資産の記録がまだありません");

        // 資産が無い時点で止まるため、生活費は取りに行かない
        verifyNoInteractions(expenseSnapshotService);
    }

    @Test
    @DisplayName("生活費の記録が無ければ例外がそのまま伝わる")
    void simulateThrowsWhenExpenseNotFound() {
        // 準備
        given(assetSnapshotService.findLatest()).willReturn(
                new AssetSnapshotResponse(1L, ASSETS_RECORDED_ON, 1_050_000L, 520_000L));
        given(expenseSnapshotService.findLatest())
                .willThrow(new ResourceNotFoundException("生活費の記録がまだありません"));

        // 実行・検証
        assertThatThrownBy(() -> survivalSimulationService.simulate())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("生活費の記録がまだありません");
    }
}
