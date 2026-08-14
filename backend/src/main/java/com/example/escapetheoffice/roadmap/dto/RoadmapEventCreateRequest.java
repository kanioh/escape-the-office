package com.example.escapetheoffice.roadmap.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// id と userId は受け取らない。DB とサーバー側で決めるため、送れる項目に含めない
public record RoadmapEventCreateRequest(

        // @NotBlank は null と空文字に加えて空白だけの文字列も弾く。
        // 200 は DB の VARCHAR(200) と同条件
        @NotBlank @Size(max = 200) String title,

        // これからの予定なので未来日が本来の使い方。実績を記録する資産・生活費と違い
        // @PastOrPresent は付けない
        @NotNull LocalDate startDate,

        // 終了未定を表せるよう、ここだけ必須にしない。
        // 開始日との前後関係は1項目では検証できないため次回まとめて入れる
        LocalDate endDate) {
}
