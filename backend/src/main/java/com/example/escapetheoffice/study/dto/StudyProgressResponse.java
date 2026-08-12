package com.example.escapetheoffice.study.dto;

import com.example.escapetheoffice.study.StudyStatus;

/**
 * 学習項目ごとの進捗。
 * 進捗が未登録の項目も NOT_STARTED / 0% として返すため、
 * Entity 1件からの変換にならない。組み立ては Service が行う。
 */
public record StudyProgressResponse(
        Long studyItemId,
        String studyItemName,
        StudyStatus status,
        int progressPercent) {
}
