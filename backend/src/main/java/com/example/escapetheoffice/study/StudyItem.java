package com.example.escapetheoffice.study;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_items")
public class StudyItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // JPA がリフレクションでインスタンスを生成するために必須。
    // アプリ側から新規作成させないよう protected にする（マスタは参照専用）
    protected StudyItem() {
    }

    // テストで採番済みの状態を再現するためだけのもの。
    // 本番で id を自分で決めることは無いため、パッケージ外へは公開しない
    StudyItem(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
