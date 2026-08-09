package com.example.escapetheoffice.expense.dto;

import java.time.LocalDate;

import com.example.escapetheoffice.expense.ExpenseSnapshot;

// userId は MVP では常に 1 で意味を持たないため返さない
public record ExpenseSnapshotResponse(
        Long id,
        LocalDate recordedOn,
        Long monthlyExpense) {

    public static ExpenseSnapshotResponse from(ExpenseSnapshot expenseSnapshot) {
        return new ExpenseSnapshotResponse(
                expenseSnapshot.getId(),
                expenseSnapshot.getRecordedOn(),
                expenseSnapshot.getMonthlyExpense());
    }
}
