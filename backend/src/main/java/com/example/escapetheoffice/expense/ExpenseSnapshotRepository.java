package com.example.escapetheoffice.expense;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseSnapshotRepository extends JpaRepository<ExpenseSnapshot, Long> {

    // メソッド名から SQL が生成される。
    boolean existsByUserIdAndRecordedOn(Long userId, LocalDate recordedOn);

    List<ExpenseSnapshot> findAllByUserIdOrderByRecordedOnDesc(Long userId);

    // First で LIMIT 1 が付く。降順の先頭 = 最新1件
    Optional<ExpenseSnapshot> findFirstByUserIdOrderByRecordedOnDesc(Long userId);
}
