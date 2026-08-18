package com.example.escapetheoffice.asset;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;
import com.example.escapetheoffice.asset.dto.AssetSnapshotUpdateRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;

@RestController
@RequestMapping("/api/assets")
public class AssetSnapshotController {

    private final AssetSnapshotService assetSnapshotService;

    public AssetSnapshotController(AssetSnapshotService assetSnapshotService) {
        this.assetSnapshotService = assetSnapshotService;
    }

    /**
     * その日の資産を登録・更新する（UPSERT）。
     * 日付がこの記録を特定する鍵のため URL に置く。新規と更新で結果は同じなので 200 に統一する。
     */
    @PutMapping("/{recordedOn}")
    public AssetSnapshotResponse update(
            // URL 上はただの文字列。ISO 形式（yyyy-MM-dd）として解釈するよう明示する。
            // 実績の記録なので未来日は受け付けない
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PastOrPresent
            LocalDate recordedOn,
            @Valid @RequestBody AssetSnapshotUpdateRequest request) {

        return assetSnapshotService.update(recordedOn, request);
    }

    @GetMapping
    public List<AssetSnapshotResponse> findAll() {
        return assetSnapshotService.findAll();
    }

    @GetMapping("/latest")
    public AssetSnapshotResponse findLatest() {
        return assetSnapshotService.findLatest();
    }
}
