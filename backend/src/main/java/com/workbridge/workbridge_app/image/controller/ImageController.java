package com.workbridge.workbridge_app.image.controller;

import java.net.URL;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.workbridge.workbridge_app.common.response.ApiResponse;
import com.workbridge.workbridge_app.common.response.ResponseFactory;
import com.workbridge.workbridge_app.image.dto.UploadResponseDTO;
import com.workbridge.workbridge_app.image.service.ImageStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageStorageService imageStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UploadResponseDTO>> upload(@RequestParam("file") MultipartFile file) {
        return ResponseFactory.ok(
            imageStorageService.uploadImage(file),
            "Image uploaded successfully."
        );
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        imageStorageService.deleteImage(key);
        return ResponseEntity.noContent().build();
    }
}
