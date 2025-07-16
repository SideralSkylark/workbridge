package com.workbridge.workbridge_app.image.service;

import org.springframework.web.multipart.MultipartFile;

import com.workbridge.workbridge_app.image.dto.GetImageResponseDTO;
import com.workbridge.workbridge_app.image.dto.UploadResponseDTO;

public interface ImageStorageService {
    UploadResponseDTO uploadImage(MultipartFile file);
    void deleteImage(String key);
    GetImageResponseDTO getImageUrl(String key);
}
