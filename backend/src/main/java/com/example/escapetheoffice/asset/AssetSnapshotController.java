package com.example.escapetheoffice.asset;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.asset.dto.AssetSnapshotCreateRequest;
import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;

@RestController
@RequestMapping("/api/assets")
public class AssetSnapshotController {

    private final AssetSnapshotService assetSnapshotService;

    public AssetSnapshotController(AssetSnapshotService assetSnapshotService) {
        this.assetSnapshotService = assetSnapshotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetSnapshotResponse create(@RequestBody AssetSnapshotCreateRequest request) {
        return assetSnapshotService.create(request);
    }
}
