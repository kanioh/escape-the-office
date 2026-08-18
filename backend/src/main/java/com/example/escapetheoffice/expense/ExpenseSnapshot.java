package com.example.escapetheoffice.expense;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "expense_snapshots")
public class ExpenseSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MVP は認証を持たないため、users への関連は張らず id をそのまま持つ
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recorded_on", nullable = false)
    private LocalDate recordedOn;

    @Column(name = "monthly_expense", nullable = false)
    private Long monthlyExpense;

    // created_at は DB の DEFAULT now() に任せるため、Entity には持たせない

    // JPA がリフレクションでインスタンスを生成するために必須
    protected ExpenseSnapshot() {
    }

    // id は DB が採番するため受け取らない。save() 後に書き戻される
    public ExpenseSnapshot(Long userId, LocalDate recordedOn, Long monthlyExpense) {
        this.userId = userId;
        this.recordedOn = recordedOn;
        this.monthlyExpense = monthlyExpense;
    }

    /**
     * 生活費を置き換える。
     * recordedOn は (user_id, recorded_on) のユニーク制約でこの記録を特定する鍵であり、
     * URL で指定するものでもあるため変更対象にしない。
     */
    public void updateMonthlyExpense(Long monthlyExpense) {
        this.monthlyExpense = monthlyExpense;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDate getRecordedOn() {
        return recordedOn;
    }

    public Long getMonthlyExpense() {
        return monthlyExpense;
    }
}
