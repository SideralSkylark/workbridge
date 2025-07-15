package com.workbridge.workbridge_app.image.service;

import java.net.URL;

import org.springframework.web.multipart.MultipartFile;

import com.workbridge.workbridge_app.image.dto.UploadResponseDTO;

public interface ImageStorageService {
    UploadResponseDTO uploadImage(MultipartFile file);
    void deleteImage(String key);
    URL getImageUrl(String key);
}
