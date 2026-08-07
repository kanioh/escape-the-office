package com.example.escapetheoffice.asset;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "asset_snapshots")
public class AssetSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MVP は認証を持たないため、users への関連は張らず id をそのまま持つ
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recorded_on", nullable = false)
    private LocalDate recordedOn;

    @Column(name = "cash_amount", nullable = false)
    private Long cashAmount;

    @Column(name = "nisa_amount", nullable = false)
    private Long nisaAmount;

    // created_at は DB の DEFAULT now() に任せるため、Entity には持たせない

    // JPA がリフレクションでインスタンスを生成するために必須
    protected AssetSnapshot() {
    }

    // id は DB が採番するため受け取らない。save() 後に書き戻される
    public AssetSnapshot(Long userId, LocalDate recordedOn, Long cashAmount, Long nisaAmount) {
        this.userId = userId;
        this.recordedOn = recordedOn;
        this.cashAmount = cashAmount;
        this.nisaAmount = nisaAmount;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDate getRecordedOn() {
        return recordedOn;
    }

    public Long getCashAmount() {
        return cashAmount;
    }

    public Long getNisaAmount() {
        return nisaAmount;
    }
}
