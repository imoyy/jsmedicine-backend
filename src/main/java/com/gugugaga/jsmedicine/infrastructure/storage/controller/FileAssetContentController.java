package com.gugugaga.jsmedicine.infrastructure.storage.controller;

import com.gugugaga.jsmedicine.infrastructure.storage.StorageObjectStream;
import com.gugugaga.jsmedicine.infrastructure.storage.service.FileAssetContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@Tag(name = "公共文件资源")
@RestController
@RequestMapping("/api/v1/files")
public class FileAssetContentController {

    private final FileAssetContentService fileAssetContentService;

    public FileAssetContentController(FileAssetContentService fileAssetContentService) {
        this.fileAssetContentService = fileAssetContentService;
    }

    @Operation(summary = "读取公开图片资源内容")
    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> content(@PathVariable Long id) {
        StorageObjectStream objectStream = fileAssetContentService.loadPublicContent(id);
        MediaType mediaType = resolveMediaType(objectStream.contentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(objectStream.contentLength())
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                .body(new InputStreamResource(objectStream.inputStream()));
    }

    private MediaType resolveMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
