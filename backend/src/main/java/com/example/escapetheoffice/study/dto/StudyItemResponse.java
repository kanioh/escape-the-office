package com.example.escapetheoffice.study.dto;

import com.example.escapetheoffice.study.StudyItem;

public record StudyItemResponse(Long id, String name) {

    // Entity から DTO への変換をここに集約し、詰め替え漏れを防ぐ
    public static StudyItemResponse from(StudyItem studyItem) {
        return new StudyItemResponse(studyItem.getId(), studyItem.getName());
    }
}
