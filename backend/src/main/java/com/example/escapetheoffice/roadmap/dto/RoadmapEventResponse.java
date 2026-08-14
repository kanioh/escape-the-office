package com.example.escapetheoffice.roadmap.dto;

import java.time.LocalDate;

import com.example.escapetheoffice.roadmap.RoadmapEvent;

// userId は MVP では常に 1 で意味を持たないため返さない
public record RoadmapEventResponse(
        Long id,
        String title,
        LocalDate startDate,

        // 終了未定の場合は "endDate": null として出力される
        LocalDate endDate) {

    // Entity 1件をそのまま詰め替えるだけなので DTO 側に置く。
    // 既定値で埋めるような判断が入る場合は Service 側に置く
    public static RoadmapEventResponse from(RoadmapEvent roadmapEvent) {
        return new RoadmapEventResponse(
                roadmapEvent.getId(),
                roadmapEvent.getTitle(),
                roadmapEvent.getStartDate(),
                roadmapEvent.getEndDate());
    }
}
