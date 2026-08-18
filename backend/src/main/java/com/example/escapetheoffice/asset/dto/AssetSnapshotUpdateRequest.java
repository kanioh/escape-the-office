package com.example.escapetheoffice.asset.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

// recordedOn は URL で受け取るため body には含めない。
// id と userId は DB とサーバー側で決めるため、送れる項目に含めない
public record AssetSnapshotUpdateRequest(

        // 0以上は DB の CHECK 制約と同条件。アプリ側でも見て分かりやすいエラーを返す
        @NotNull @PositiveOrZero Long cashAmount,

        @NotNull @PositiveOrZero Long nisaAmount) {
}
