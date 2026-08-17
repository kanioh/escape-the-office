package com.example.escapetheoffice.roadmap.dto;

import java.time.LocalDate;

import com.example.escapetheoffice.common.validation.DateRange;
import com.example.escapetheoffice.common.validation.ValidDateRange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// 現時点の項目は RoadmapEventCreateRequest と同じだが、意図して別ファイルにしている。
// 共有すると「登録時のみ必須」のような片側だけの仕様変更ができなくなるため。
// id は URL で受け取るため body には含めない
@ValidDateRange
public record RoadmapEventUpdateRequest(

        @NotBlank @Size(max = 200) String title,

        @NotNull LocalDate startDate,

        // 終了未定に戻せるよう必須にしない
        LocalDate endDate) implements DateRange {
}
