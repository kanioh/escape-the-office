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

        // 資産または生活費が未登録なら null。複数の情報を束ねる画面なので、
        // 一部が欠けても画面ごと 404 にはせず、その項目だけ空で返す。
        // 0 は「資産0円」と区別がつかないため、プリミティブの long ではなく Long を使う
        Long totalAssets,
        Long survivableMonths,

        // 最近更新した順の数件だけ。全件は画面③（学習管理）で見る。
        // 一度も進捗を登録していない項目は updated_at を持たないため含まれない
        List<StudyProgressResponse> recentStudyProgress,

        // これから始まる予定が1件も無ければ null
        RoadmapEventResponse nextEvent) {
}
