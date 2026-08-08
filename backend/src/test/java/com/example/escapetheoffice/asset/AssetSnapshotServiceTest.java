package com.example.escapetheoffice.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.escapetheoffice.asset.dto.AssetSnapshotCreateRequest;
import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;
import com.example.escapetheoffice.common.exception.DuplicateResourceException;

@ExtendWith(MockitoExtension.class)
class AssetSnapshotServiceTest {

    // 同じ値を何度も書くと片方だけ直して食い違うため定数にする
    private static final long USER_ID = 1L;
    private static final LocalDate RECORDED_ON = LocalDate.of(2026, 8, 8);
    private static final long CASH_AMOUNT = 1_000_000L;
    private static final long NISA_AMOUNT = 500_000L;

    @Mock
    private AssetSnapshotRepository assetSnapshotRepository;

    @InjectMocks
    private AssetSnapshotService assetSnapshotService;

    @Test
    @DisplayName("同じ日付の記録が無ければ登録できる")
    void createSavesWhenNotDuplicated() {
        // 準備
        AssetSnapshotCreateRequest request =
                new AssetSnapshotCreateRequest(RECORDED_ON, CASH_AMOUNT, NISA_AMOUNT);
        given(assetSnapshotRepository.existsByUserIdAndRecordedOn(eq(USER_ID), eq(RECORDED_ON)))
                .willReturn(false);
        given(assetSnapshotRepository.save(any(AssetSnapshot.class)))
                .willReturn(new AssetSnapshot(USER_ID, RECORDED_ON, CASH_AMOUNT, NISA_AMOUNT));

        // 実行
        AssetSnapshotResponse response = assetSnapshotService.create(request);

        // 検証
        assertThat(response.recordedOn()).isEqualTo(RECORDED_ON);
        assertThat(response.cashAmount()).isEqualTo(CASH_AMOUNT);
        assertThat(response.nisaAmount()).isEqualTo(NISA_AMOUNT);
        verify(assetSnapshotRepository).save(any(AssetSnapshot.class));
    }

    @Test
    @DisplayName("同じ日付の記録があれば例外を投げ、保存しない")
    void createThrowsWhenDuplicated() {
        // 準備
        AssetSnapshotCreateRequest request =
                new AssetSnapshotCreateRequest(RECORDED_ON, CASH_AMOUNT, NISA_AMOUNT);
        given(assetSnapshotRepository.existsByUserIdAndRecordedOn(eq(USER_ID), eq(RECORDED_ON)))
                .willReturn(true);

        // 実行・検証
        assertThatThrownBy(() -> assetSnapshotService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("2026-08-08");

        verify(assetSnapshotRepository, never()).save(any(AssetSnapshot.class));
    }
}
