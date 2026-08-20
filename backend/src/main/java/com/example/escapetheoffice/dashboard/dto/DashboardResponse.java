package com.example.escapetheoffice.dashboard.dto;

import java.util.List;

import com.example.escapetheoffice.roadmap.dto.RoadmapEventResponse;
import com.example.escapetheoffice.study.dto.StudyProgressResponse;

/**
 * ダッシュボード（画面①）が必要とするデータをまとめて返す。
 * 専用のテーブルは持たず、既存の Service の結果を組み合わせるだけ。
 *
 * <p>学習進捗と次の目標は既存の DTO をそのまま入れる。同じ意味の型を2つ作らないため。
 * 一方シミュレーションは basedOn（計算根拠の日付）がこの画面では不要なので、使う2つだけ取り出す。
 */
public record DashboardResponse(

        // 資産と生活費は必ず登録されている前提。無ければ画面ごと 404 になるため null は無い
        long totalAssets,
        long survivableMonths,

        // 未登録の学習項目も 0% で埋まるため、常に全項目が並ぶ
        List<StudyProgressResponse> studyProgress,

        // これから始まる予定が1件も無ければ null
        RoadmapEventResponse nextEvent) {
}
