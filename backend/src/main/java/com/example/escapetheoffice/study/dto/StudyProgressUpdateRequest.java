package com.example.escapetheoffice.study.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// studyItemId は URL で受け取るため body には含めない。
// status は progressPercent から導出するため受け取らない（矛盾した組み合わせを防ぐ）
public record StudyProgressUpdateRequest(

        // 0〜100 は DB の CHECK 制約と同条件。アプリ側でも見て分かりやすいエラーを返す
        @NotNull @Min(0) @Max(100) Integer progressPercent) {
}
