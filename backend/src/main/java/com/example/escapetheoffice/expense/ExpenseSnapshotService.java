package com.example.escapetheoffice.expense;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.common.exception.DuplicateResourceException;
import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotCreateRequest;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotResponse;

@Service
public class ExpenseSnapshotService {

    // MVP は認証を持たないため固定。認証導入時はログインユーザーから取得する処理に置き換える
    private static final long CURRENT_USER_ID = 1L;

    private final ExpenseSnapshotRepository expenseSnapshotRepository;

    public ExpenseSnapshotService(ExpenseSnapshotRepository expenseSnapshotRepository) {
        this.expenseSnapshotRepository = expenseSnapshotRepository;
    }

    @Transactional
    public ExpenseSnapshotResponse create(ExpenseSnapshotCreateRequest request) {
        // 同一日の二重登録を防ぐ。競合時の最後の砦は DB のユニーク制約
        if (expenseSnapshotRepository.existsByUserIdAndRecordedOn(CURRENT_USER_ID, request.recordedOn())) {
            throw new DuplicateResourceException(
                    request.recordedOn() + " の生活費は既に登録されています");
        }

        ExpenseSnapshot expenseSnapshot = new ExpenseSnapshot(
                CURRENT_USER_ID,
                request.recordedOn(),
                request.monthlyExpense());

        // save() の戻り値には採番された id が入っているため、引数ではなく戻り値を使う
        ExpenseSnapshot saved = expenseSnapshotRepository.save(expenseSnapshot);

        return ExpenseSnapshotResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseSnapshotResponse> findAll() {
        return expenseSnapshotRepository.findAllByUserIdOrderByRecordedOnDesc(CURRENT_USER_ID).stream()
                .map(ExpenseSnapshotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseSnapshotResponse findLatest() {
        return expenseSnapshotRepository.findFirstByUserIdOrderByRecordedOnDesc(CURRENT_USER_ID)
                .map(ExpenseSnapshotResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("生活費の記録がまだありません"));
    }
}
