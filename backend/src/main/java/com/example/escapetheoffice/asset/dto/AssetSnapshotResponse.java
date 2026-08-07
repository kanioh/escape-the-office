package com.example.escapetheoffice.asset.dto;

import java.time.LocalDate;

import com.example.escapetheoffice.asset.AssetSnapshot;

// userId は MVP では常に 1 で意味を持たないため返さない
public record AssetSnapshotResponse(
        Long id,
        LocalDate recordedOn,
        Long cashAmount,
        Long nisaAmount) {

    public static AssetSnapshotResponse from(AssetSnapshot assetSnapshot) {
        return new AssetSnapshotResponse(
                assetSnapshot.getId(),
                assetSnapshot.getRecordedOn(),
                assetSnapshot.getCashAmount(),
                assetSnapshot.getNisaAmount());
    }
}
