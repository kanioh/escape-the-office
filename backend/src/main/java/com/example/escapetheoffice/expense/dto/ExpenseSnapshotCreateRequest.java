package com.example.escapetheoffice.expense.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

// id と userId は受け取らない。DB とサーバー側で決めるため、送れる項目に含めない
public record ExpenseSnapshotCreateRequest(

        // 実績の記録なので未来日は受け付けない
        @NotNull @PastOrPresent LocalDate recordedOn,

        // 生活費が0はありえないので、必ず正の数
        @NotNull @Positive Long monthlyExpense) {
}
