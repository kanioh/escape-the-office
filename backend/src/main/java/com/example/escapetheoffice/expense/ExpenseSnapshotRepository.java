package com.example.escapetheoffice.expense;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseSnapshotRepository extends JpaRepository<ExpenseSnapshot, Long> {

    // メソッド名から SQL が生成される。
    // 未登録は異常ではなく新規作成に進む合図のため、Optional で受ける
    Optional<ExpenseSnapshot> findByUserIdAndRecordedOn(Long userId, LocalDate recordedOn);

    List<ExpenseSnapshot> findAllByUserIdOrderByRecordedOnDesc(Long userId);

    // First で LIMIT 1 が付く。降順の先頭 = 最新1件
    Optional<ExpenseSnapshot> findFirstByUserIdOrderByRecordedOnDesc(Long userId);
}
