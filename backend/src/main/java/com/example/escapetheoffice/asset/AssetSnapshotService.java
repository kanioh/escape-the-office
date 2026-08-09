package com.example.escapetheoffice.asset;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.asset.dto.AssetSnapshotCreateRequest;
import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;
import com.example.escapetheoffice.common.exception.DuplicateResourceException;
import com.example.escapetheoffice.common.exception.ResourceNotFoundException;

@Service
public class AssetSnapshotService {

    // MVP は認証を持たないため固定。認証導入時はログインユーザーから取得する処理に置き換える
    private static final long CURRENT_USER_ID = 1L;

    private final AssetSnapshotRepository assetSnapshotRepository;

    public AssetSnapshotService(AssetSnapshotRepository assetSnapshotRepository) {
        this.assetSnapshotRepository = assetSnapshotRepository;
    }

    @Transactional
    public AssetSnapshotResponse create(AssetSnapshotCreateRequest request) {
        // 同一日の二重登録を防ぐ。競合時の最後の砦は DB のユニーク制約
        if (assetSnapshotRepository.existsByUserIdAndRecordedOn(CURRENT_USER_ID, request.recordedOn())) {
            throw new DuplicateResourceException(
                    request.recordedOn() + " の資産は既に登録されています");
        }

        AssetSnapshot assetSnapshot = new AssetSnapshot(
                CURRENT_USER_ID,
                request.recordedOn(),
                request.cashAmount(),
                request.nisaAmount());

        // save() の戻り値には採番された id が入っているため、引数ではなく戻り値を使う
        AssetSnapshot saved = assetSnapshotRepository.save(assetSnapshot);

        return AssetSnapshotResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AssetSnapshotResponse> findAll() {
        return assetSnapshotRepository.findAllByUserIdOrderByRecordedOnDesc(CURRENT_USER_ID).stream()
                .map(AssetSnapshotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssetSnapshotResponse findLatest() {
        return assetSnapshotRepository.findFirstByUserIdOrderByRecordedOnDesc(CURRENT_USER_ID)
                .map(AssetSnapshotResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("資産の記録がまだありません"));
    }
}
