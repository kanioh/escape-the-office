package com.example.escapetheoffice.expense;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.common.exception.ResourceNotFoundException;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotResponse;
import com.example.escapetheoffice.expense.dto.ExpenseSnapshotUpdateRequest;

@Service
public class ExpenseSnapshotService {

    // MVP は認証を持たないため固定。認証導入時はログインユーザーから取得する処理に置き換える
    private static final long CURRENT_USER_ID = 1L;

    private final ExpenseSnapshotRepository expenseSnapshotRepository;

    public ExpenseSnapshotService(ExpenseSnapshotRepository expenseSnapshotRepository) {
        this.expenseSnapshotRepository = expenseSnapshotRepository;
    }

    /**
     * その日の生活費を登録する。既に記録があれば上書きする（UPSERT）。
     * 生活費は設定値の見直しなので、同じ日に何度も直したくなる。
     */
    @Transactional
    public ExpenseSnapshotResponse update(
            LocalDate recordedOn, ExpenseSnapshotUpdateRequest request) {

        Optional<ExpenseSnapshot> existing =
                expenseSnapshotRepository.findByUserIdAndRecordedOn(CURRENT_USER_ID, recordedOn);

        ExpenseSnapshot expenseSnapshot;
        if (existing.isPresent()) {
            expenseSnapshot = existing.get();
            // 取得済みの Entity は JPA が追跡しているため、値を変えるだけで
            // トランザクション終了時に UPDATE が発行される（ダーティチェック）
            expenseSnapshot.updateMonthlyExpense(request.monthlyExpense());
        } else {
            // new しただけの Entity は JPA の管理外なので、save() で管理下に入れる
            expenseSnapshot = expenseSnapshotRepository.save(new ExpenseSnapshot(
                    CURRENT_USER_ID,
                    recordedOn,
                    request.monthlyExpense()));
        }

        return ExpenseSnapshotResponse.from(expenseSnapshot);
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
