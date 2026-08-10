package com.example.escapetheoffice.simulation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.asset.AssetSnapshotService;
import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;
import com.example.escapetheoffice.expense.ExpenseSnapshotService;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotResponse;
import com.example.escapetheoffice.simulation.dto.SurvivalSimulationResponse;

/**
 * 資産と生活費から「あと何ヶ月生活できるか」を計算する。
 * 専用のテーブルを持たず、既存の Service を組み合わせるだけなので Repository は持たない。
 */
@Service
public class SurvivalSimulationService {

    private final AssetSnapshotService assetSnapshotService;
    private final ExpenseSnapshotService expenseSnapshotService;

    public SurvivalSimulationService(
            AssetSnapshotService assetSnapshotService,
            ExpenseSnapshotService expenseSnapshotService) {
        this.assetSnapshotService = assetSnapshotService;
        this.expenseSnapshotService = expenseSnapshotService;
    }

    @Transactional(readOnly = true)
    public SurvivalSimulationResponse simulate() {
        // どちらか一方でも記録が無ければ findLatest() が例外を投げ、404 になる
        AssetSnapshotResponse asset = assetSnapshotService.findLatest();
        ExpenseSnapshotResponse expense = expenseSnapshotService.findLatest();

        // NISA も売れば生活費に回せるため資産に含める
        long totalAssets = asset.cashAmount() + asset.nisaAmount();

        // 整数同士の除算は小数が捨てられる。安全側に見積もるため切り捨てで良い
        long survivableMonths = totalAssets / expense.monthlyExpense();

        return new SurvivalSimulationResponse(
                totalAssets,
                expense.monthlyExpense(),
                survivableMonths,
                new SurvivalSimulationResponse.BasedOn(
                        asset.recordedOn(),
                        expense.recordedOn()));
    }
}
