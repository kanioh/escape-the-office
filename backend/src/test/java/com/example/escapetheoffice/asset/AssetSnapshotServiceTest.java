package com.example.escapetheoffice.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;
import com.example.escapetheoffice.asset.dto.AssetSnapshotUpdateRequest;
import com.example.escapetheoffice.common.exception.ResourceNotFoundException;

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
    @DisplayName("その日の記録が無ければ新規作成して保存する")
    void updateCreatesSnapshotWhenNotRegistered() {
        // 準備
        given(assetSnapshotRepository.findByUserIdAndRecordedOn(USER_ID, RECORDED_ON))
                .willReturn(Optional.empty());
        // 保存された Entity がそのまま返る、という実際のリポジトリの挙動を再現する。
        // この台本が使われなければテストは失敗するため、save が呼ばれたことも保証される
        given(assetSnapshotRepository.save(any(AssetSnapshot.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // 実行
        AssetSnapshotResponse response = assetSnapshotService.update(
                RECORDED_ON, new AssetSnapshotUpdateRequest(CASH_AMOUNT, NISA_AMOUNT));

        // 検証
        assertThat(response.recordedOn()).isEqualTo(RECORDED_ON);
        assertThat(response.cashAmount()).isEqualTo(CASH_AMOUNT);
        assertThat(response.nisaAmount()).isEqualTo(NISA_AMOUNT);
    }

    @Test
    @DisplayName("その日の記録があればダーティチェックに任せ、save を呼ばずに上書きする")
    void updateOverwritesExistingSnapshotWithoutSave() {
        // 準備
        AssetSnapshot existing =
                new AssetSnapshot(USER_ID, RECORDED_ON, CASH_AMOUNT, NISA_AMOUNT);
        given(assetSnapshotRepository.findByUserIdAndRecordedOn(USER_ID, RECORDED_ON))
                .willReturn(Optional.of(existing));

        // 実行
        AssetSnapshotResponse response = assetSnapshotService.update(
                RECORDED_ON, new AssetSnapshotUpdateRequest(1_200_000L, 600_000L));

        // 検証
        assertThat(response.cashAmount()).isEqualTo(1_200_000L);
        assertThat(response.nisaAmount()).isEqualTo(600_000L);
        // 日付は鍵なので変わらない
        assertThat(response.recordedOn()).isEqualTo(RECORDED_ON);
        // Entity 自体が書き換わっていることを確認する
        assertThat(existing.getCashAmount()).isEqualTo(1_200_000L);
        // save を呼ばないのは意図した実装。呼ぶよう変えたらここで気付ける
        verify(assetSnapshotRepository, never()).save(any());
    }

    @Test
    @DisplayName("一覧は記録を DTO に変換して返す")
    void findAllReturnsResponses() {
        // 準備
        given(assetSnapshotRepository.findAllByUserIdOrderByRecordedOnDesc(eq(USER_ID)))
                .willReturn(List.of(
                        new AssetSnapshot(USER_ID, RECORDED_ON, CASH_AMOUNT, NISA_AMOUNT),
                        new AssetSnapshot(USER_ID, RECORDED_ON.minusDays(1), 900_000L, 400_000L)));

        // 実行
        List<AssetSnapshotResponse> responses = assetSnapshotService.findAll();

        // 検証
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).recordedOn()).isEqualTo(RECORDED_ON);
        assertThat(responses.get(1).cashAmount()).isEqualTo(900_000L);
    }

    @Test
    @DisplayName("記録があれば最新1件を返す")
    void findLatestReturnsResponse() {
        // 準備
        given(assetSnapshotRepository.findFirstByUserIdOrderByRecordedOnDesc(eq(USER_ID)))
                .willReturn(Optional.of(
                        new AssetSnapshot(USER_ID, RECORDED_ON, CASH_AMOUNT, NISA_AMOUNT)));

        // 実行
        AssetSnapshotResponse response = assetSnapshotService.findLatest();

        // 検証
        assertThat(response.recordedOn()).isEqualTo(RECORDED_ON);
        assertThat(response.cashAmount()).isEqualTo(CASH_AMOUNT);
    }

    @Test
    @DisplayName("記録が1件も無ければ例外を投げる")
    void findLatestThrowsWhenEmpty() {
        // 準備
        given(assetSnapshotRepository.findFirstByUserIdOrderByRecordedOnDesc(eq(USER_ID)))
                .willReturn(Optional.empty());

        // 実行・検証
        assertThatThrownBy(() -> assetSnapshotService.findLatest())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("資産の記録がまだありません");
    }
}
