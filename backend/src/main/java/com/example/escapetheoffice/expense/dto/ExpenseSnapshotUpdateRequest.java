package com.example.escapetheoffice.expense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// recordedOn は URL で受け取るため body には含めない。
// id と userId は DB とサーバー側で決めるため、送れる項目に含めない
public record ExpenseSnapshotUpdateRequest(

        // 生活費が0はありえないので、必ず正の数
        @NotNull @Positive Long monthlyExpense) {
}
