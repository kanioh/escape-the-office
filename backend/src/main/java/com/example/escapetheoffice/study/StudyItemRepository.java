package com.example.escapetheoffice.study;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyItemRepository extends JpaRepository<StudyItem, Long> {

    // メソッド名から SELECT が組み立てられる（SELECT ... WHERE name = ? の存在確認）
    boolean existsByName(String name);
}
