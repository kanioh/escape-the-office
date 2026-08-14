package com.example.escapetheoffice.study;

import java.time.OffsetDateTime;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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

    // created_at と違い DB の DEFAULT では UPDATE 時に更新されないため、Entity 側で面倒を見る
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // JPA がリフレクションでインスタンスを生成するために必須
    protected StudyProgress() {
    }

    // id は DB が採番するため受け取らない。
    // status は progressPercent から決まるため受け取らず、常に整合した状態で生成する
    public StudyProgress(Long userId, StudyItem studyItem, Integer progressPercent) {
        this.userId = userId;
        this.studyItem = studyItem;
        this.progressPercent = progressPercent;
        this.status = resolveStatus(progressPercent);
    }

    /**
     * 進捗率を更新する。
     * status を単独で書き換えられると進捗率と食い違うため、setter は設けず必ずここを通す。
     */
    public void changeProgress(Integer progressPercent) {
        this.progressPercent = progressPercent;
        this.status = resolveStatus(progressPercent);
    }

    /** 進捗率とステータスは同じ事実の別表現なので、片方から機械的に決める */
    private static StudyStatus resolveStatus(Integer progressPercent) {
        if (progressPercent == 0) {
            return StudyStatus.NOT_STARTED;
        }
        if (progressPercent == 100) {
            return StudyStatus.DONE;
        }
        return StudyStatus.IN_PROGRESS;
    }

    // INSERT / UPDATE の直前に JPA が呼ぶ。両方に付けないと初回保存で null のまま送られる
    @PrePersist
    @PreUpdate
    void touchUpdatedAt() {
        this.updatedAt = OffsetDateTime.now();
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
