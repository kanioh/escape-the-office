package com.example.escapetheoffice.study;

/**
 * 学習の進捗ステータス。
 * DB は VARCHAR + CHECK で同じ3値を持つ（V3__create_study_tables.sql）。
 */
public enum StudyStatus {

    NOT_STARTED,
    IN_PROGRESS,
    DONE
}
