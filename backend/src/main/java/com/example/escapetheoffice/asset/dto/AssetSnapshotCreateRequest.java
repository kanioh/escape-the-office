package com.example.escapetheoffice.asset.dto;

import java.time.LocalDate;

// id と userId は受け取らない。DB とサーバー側で決めるため、送れる項目に含めない
public record AssetSnapshotCreateRequest(
        LocalDate recordedOn,
        Long cashAmount,
        Long nisaAmount) {
}
