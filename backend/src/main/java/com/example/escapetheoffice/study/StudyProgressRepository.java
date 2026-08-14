package com.example.escapetheoffice.study;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyProgressRepository extends JpaRepository<StudyProgress, Long> {

    // 並び順は Service で学習項目マスタの順に組み立てるため、ここでは指定しない
    List<StudyProgress> findAllByUserId(Long userId);

    // 未登録は異常ではなく新規作成に進む正常系のため、null ではなく Optional で受ける。
    // StudyItemId と書くと関連先 StudyItem の id で絞り込まれる
    Optional<StudyProgress> findByUserIdAndStudyItemId(Long userId, Long studyItemId);
}
