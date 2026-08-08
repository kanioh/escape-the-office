package com.example.escapetheoffice.asset;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetSnapshotRepository extends JpaRepository<AssetSnapshot, Long> {

    // メソッド名から SQL が生成される。uq_asset_snapshots_user_date と同じ条件
    boolean existsByUserIdAndRecordedOn(Long userId, LocalDate recordedOn);
}
