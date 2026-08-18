package com.example.escapetheoffice.asset;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;
import com.example.escapetheoffice.asset.dto.AssetSnapshotUpdateRequest;
import com.example.escapetheoffice.common.exception.ResourceNotFoundException;

@Service
public class AssetSnapshotService {

    // MVP は認証を持たないため固定。認証導入時はログインユーザーから取得する処理に置き換える
    private static final long CURRENT_USER_ID = 1L;

    private final AssetSnapshotRepository assetSnapshotRepository;

    public AssetSnapshotService(AssetSnapshotRepository assetSnapshotRepository) {
        this.assetSnapshotRepository = assetSnapshotRepository;
    }

    /**
     * その日の資産を登録する。既に記録があれば上書きする（UPSERT）。
     * 残高の入力ミスはその場で直したくなるため、同一日の再登録をエラーにしない。
     */
    @Transactional
    public AssetSnapshotResponse update(
            LocalDate recordedOn, AssetSnapshotUpdateRequest request) {

        Optional<AssetSnapshot> existing =
                assetSnapshotRepository.findByUserIdAndRecordedOn(CURRENT_USER_ID, recordedOn);

        AssetSnapshot assetSnapshot;
        if (existing.isPresent()) {
            assetSnapshot = existing.get();
            // 取得済みの Entity は JPA が追跡しているため、値を変えるだけで
            // トランザクション終了時に UPDATE が発行される（ダーティチェック）
            assetSnapshot.updateAmounts(request.cashAmount(), request.nisaAmount());
        } else {
            // new しただけの Entity は JPA の管理外なので、save() で管理下に入れる
            assetSnapshot = assetSnapshotRepository.save(new AssetSnapshot(
                    CURRENT_USER_ID,
                    recordedOn,
                    request.cashAmount(),
                    request.nisaAmount()));
        }

        return AssetSnapshotResponse.from(assetSnapshot);
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
