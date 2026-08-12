package com.example.escapetheoffice.study;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyProgressRepository extends JpaRepository<StudyProgress, Long> {

    // 並び順は Service で学習項目マスタの順に組み立てるため、ここでは指定しない
    List<StudyProgress> findAllByUserId(Long userId);
}
