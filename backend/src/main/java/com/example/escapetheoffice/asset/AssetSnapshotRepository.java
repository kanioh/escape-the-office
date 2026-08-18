package com.example.escapetheoffice.asset;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetSnapshotRepository extends JpaRepository<AssetSnapshot, Long> {

    // メソッド名から SQL が生成される。uq_asset_snapshots_user_date と同じ条件。
    // 未登録は異常ではなく新規作成に進む合図のため、Optional で受ける
    Optional<AssetSnapshot> findByUserIdAndRecordedOn(Long userId, LocalDate recordedOn);

    List<AssetSnapshot> findAllByUserIdOrderByRecordedOnDesc(Long userId);

    // First で LIMIT 1 が付く。降順の先頭 = 最新1件
    Optional<AssetSnapshot> findFirstByUserIdOrderByRecordedOnDesc(Long userId);
}
