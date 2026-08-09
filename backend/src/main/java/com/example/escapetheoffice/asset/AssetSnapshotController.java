package com.example.escapetheoffice.asset;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.escapetheoffice.asset.dto.AssetSnapshotCreateRequest;
import com.example.escapetheoffice.asset.dto.AssetSnapshotResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assets")
public class AssetSnapshotController {

    private final AssetSnapshotService assetSnapshotService;

    public AssetSnapshotController(AssetSnapshotService assetSnapshotService) {
        this.assetSnapshotService = assetSnapshotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetSnapshotResponse create(@Valid @RequestBody AssetSnapshotCreateRequest request) {
        return assetSnapshotService.create(request);
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
