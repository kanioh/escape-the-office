package com.example.escapetheoffice.simulation.dto;

import java.time.LocalDate;

/**
 * 生存期間シミュレーションの結果。
 * 対応するテーブルが無く計算結果を返すだけなので、Entity からの変換メソッドは持たない。
 * 値は必ず存在するため、null になれないプリミティブ型で受ける。
 */
public record SurvivalSimulationResponse(
        long totalAssets,
        long monthlyExpense,
        long survivableMonths,
        BasedOn basedOn) {

    /** どの時点のデータを使って計算したか。数字が古い可能性に利用者が気付けるようにする */
    public record BasedOn(
            LocalDate assetsRecordedOn,
            LocalDate expenseRecordedOn) {
    }
}
