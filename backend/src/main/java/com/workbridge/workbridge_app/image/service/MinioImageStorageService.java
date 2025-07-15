package com.workbridge.workbridge_app.image.service;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.workbridge.workbridge_app.config.minio.MinioProperties;
import com.workbridge.workbridge_app.image.dto.UploadResponseDTO;
import com.workbridge.workbridge_app.image.exception.ImageStorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioImageStorageService implements ImageStorageService {

    private final S3Client s3Client;
    private final MinioProperties properties;

    @Override
    @Retryable(
        value = {ImageStorageException.class},
        maxAttempts = 3,
        backoff = @org.springframework.retry.annotation.Backoff(delay = 2000)
    )
    public UploadResponseDTO uploadImage(MultipartFile file) {
        ensureBucketExists();

        validateFile(file);

        String key = generateUniqueKey(file);

        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(key)
            .contentType(file.getContentType())
            .build();

        try {
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            log.error("Failed to upload image to MinIO", e);
            throw new ImageStorageException("Failed to upload image", e);
        }

        URL url = s3Client.utilities().getUrl(GetUrlRequest.builder()
            .bucket(properties.getBucket())
            .key(key)
            .build());

        log.info("Image uploaded successfully: {}", url);
        return new UploadResponseDTO(url.toString());
    }

    @Override
    public void deleteImage(String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
            .bucket(properties.getBucket())
            .key(key)
            .build();

        s3Client.deleteObject(deleteRequest);
        log.info("Image deleted: {}", key);
    }

    @Override
    public URL getImageUrl(String key) {
        GetUrlRequest request = GetUrlRequest.builder()
            .bucket(properties.getBucket())
            .key(key)
            .build();

        return s3Client.utilities().getUrl(request);
    }

    private void ensureBucketExists() {
        String bucket = properties.getBucket();

        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucket).build();
            s3Client.headBucket(headBucketRequest);
        } catch (NoSuchBucketException e) {
            log.warn("Bucket '{}' does not exist. Creating it now...", bucket);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty() || file.getSize() <= 0) {
            throw new ImageStorageException("Uploaded file is empty.");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new ImageStorageException("Only image files are allowed.");
        }
    }

    private String generateUniqueKey(MultipartFile file) {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename()).replaceAll("\\s+", "_");
        String uuid = java.util.UUID.randomUUID().toString();
        return uuid + "_" + originalFilename;
    }
}
