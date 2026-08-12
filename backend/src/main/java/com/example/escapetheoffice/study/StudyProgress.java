package com.example.escapetheoffice.study;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_progress")
public class StudyProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MVP は認証を持たないため、users への関連は張らず id をそのまま持つ
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 学習項目名を常に一緒に表示するため、こちらは関連を張る。
    // @ManyToOne の既定は EAGER のため、使わない場面で取得されないよう LAZY を明示する
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_item_id", nullable = false)
    private StudyItem studyItem;

    // ORDINAL（既定）は定数の並べ替えで既存データの意味が変わるため必ず STRING にする
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StudyStatus status;

    @Column(name = "progress_percent", nullable = false)
    private Integer progressPercent;

    // updated_at は更新機能を作る際に @PreUpdate と併せて追加する

    // JPA がリフレクションでインスタンスを生成するために必須
    protected StudyProgress() {
    }

    // id は DB が採番するため受け取らない
    public StudyProgress(
            Long userId, StudyItem studyItem, StudyStatus status, Integer progressPercent) {
        this.userId = userId;
        this.studyItem = studyItem;
        this.status = status;
        this.progressPercent = progressPercent;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public StudyItem getStudyItem() {
        return studyItem;
    }

    public StudyStatus getStatus() {
        return status;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }
}
