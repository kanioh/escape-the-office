package com.example.escapetheoffice.roadmap;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roadmap_events")
public class RoadmapEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MVP は認証を持たないため、users への関連は張らず id をそのまま持つ
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // 終了未定を NULL で表すため、この列だけ nullable のままにする
    @Column(name = "end_date")
    private LocalDate endDate;

    // created_at は DB の DEFAULT now() に任せるため、Entity には持たせない

    // JPA がリフレクションでインスタンスを生成するために必須
    protected RoadmapEvent() {
    }

    // id は DB が採番するため受け取らない。save() 後に書き戻される
    public RoadmapEvent(Long userId, String title, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    /** 終了未定の場合は null を返す */
    public LocalDate getEndDate() {
        return endDate;
    }
}
