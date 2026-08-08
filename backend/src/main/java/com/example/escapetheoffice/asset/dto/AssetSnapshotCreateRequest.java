package com.example.escapetheoffice.asset.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

// id と userId は受け取らない。DB とサーバー側で決めるため、送れる項目に含めない
public record AssetSnapshotCreateRequest(

        // 実績の記録なので未来日は受け付けない
        @NotNull @PastOrPresent LocalDate recordedOn,

        // 0以上は DB の CHECK 制約と同条件。アプリ側でも見て分かりやすいエラーを返す
        @NotNull @PositiveOrZero Long cashAmount,

        @NotNull @PositiveOrZero Long nisaAmount) {
}
