package com.example.escapetheoffice.simulation;

import java.util.Optional;

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

    /**
     * 資産か生活費が未登録なら計算できないため空を返す。
     * 404 にするか欠損として扱うかは呼び出し側の判断に委ねる
     * （単体の API は 404、ダッシュボードは他の情報だけ表示する）。
     */
    @Transactional(readOnly = true)
    public Optional<SurvivalSimulationResponse> simulate() {
        // 資産が無い時点で計算不能なので、生活費は取りに行かない
        Optional<AssetSnapshotResponse> latestAsset = assetSnapshotService.findLatest();
        if (latestAsset.isEmpty()) {
            return Optional.empty();
        }

        Optional<ExpenseSnapshotResponse> latestExpense = expenseSnapshotService.findLatest();
        if (latestExpense.isEmpty()) {
            return Optional.empty();
        }

        AssetSnapshotResponse asset = latestAsset.get();
        ExpenseSnapshotResponse expense = latestExpense.get();

        // NISA も売れば生活費に回せるため資産に含める
        long totalAssets = asset.cashAmount() + asset.nisaAmount();

        // 整数同士の除算は小数が捨てられる。安全側に見積もるため切り捨てで良い
        long survivableMonths = totalAssets / expense.monthlyExpense();

        return Optional.of(new SurvivalSimulationResponse(
                totalAssets,
                expense.monthlyExpense(),
                survivableMonths,
                new SurvivalSimulationResponse.BasedOn(
                        asset.recordedOn(),
                        expense.recordedOn())));
    }
}
