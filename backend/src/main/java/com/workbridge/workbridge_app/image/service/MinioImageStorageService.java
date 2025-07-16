package com.workbridge.workbridge_app.image.service;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.workbridge.workbridge_app.config.minio.MinioProperties;
import com.workbridge.workbridge_app.image.dto.GetImageResponseDTO;
import com.workbridge.workbridge_app.image.dto.UploadResponseDTO;
import com.workbridge.workbridge_app.image.exception.ImageStorageException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * Service class for managing image storage operations using MinIO (S3-compatible).
 * <p>
 * This service provides business logic for:
 * <ul>
 *   <li>Uploading validated image files to a MinIO bucket</li>
 *   <li>Deleting images by their key from the bucket</li>
 *   <li>Generating secure, temporary presigned URLs to access images</li>
 * </ul>
 *
 * <p>All methods handle MinIO/S3 exceptions and wrap them in domain-specific {@link ImageStorageException}.</p>
 *
 * <p>Typical usage:</p>
 * <pre>
 *   imageStorageService.uploadImage(file);
 *   imageStorageService.deleteImage(key);
 *   imageStorageService.getImageUrl(key);
 * </pre>
 *
 * @author Workbridge Team
 * @since 2025-07-16
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioImageStorageService implements ImageStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final MinioProperties properties;

    @Value("${minio.presigned-expiration-minutes:10}")
    private Long presignedExpirationMinutes;

    /**
     * Uploads a validated image file to the MinIO bucket using a unique object key.
     * <p>
     * If the bucket does not exist, it is created automatically.
     *
     * @param file the {@link MultipartFile} to upload
     * @return an {@link UploadResponseDTO} containing the public URL to the stored object
     * @throws ImageStorageException if the file is invalid or upload fails
     */
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
        String bucket = properties.getBucket();

        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(file.getContentType())
            .build();

        try {
            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            log.error("Failed to upload image to bucket '{}'. Key: {}", bucket, key, e);
            throw new ImageStorageException("Failed to upload image", e);
        }

        URL url = s3Client.utilities().getUrl(GetUrlRequest.builder()
            .bucket(bucket)
            .key(key)
            .build());

        log.info("Image uploaded successfully to bucket '{}'. Key: {}, URL: {}", bucket, key, url);

        return new UploadResponseDTO(url.toString());
    }

    /**
     * Deletes an image from the configured bucket by its object key.
     *
     * @param key the unique key identifying the image in the bucket
     */
    @Override
    public void deleteImage(String key) {
        String bucket = properties.getBucket();

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        s3Client.deleteObject(deleteRequest);
        log.info("Image deleted from bucket '{}'. Key: {}", bucket, key);
    }

    /**
     * Generates a presigned URL for accessing the image.
     * <p>
     * The presigned URL allows secure access for a limited time.
     *
     * @param key the key of the image to generate a URL for
     * @return a {@link GetImageResponseDTO} containing the presigned URL
     * @throws ImageStorageException if the object does not exist
     */
    @Override
    public GetImageResponseDTO getImageUrl(String key) {
        String bucket = properties.getBucket();
        ensureObjectExists(key);

        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(presignedExpirationMinutes))
            .getObjectRequest(getRequest)
            .build();

        String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();

        log.info("Generated presigned URL for key '{}' in bucket '{}'. Expires in {} minutes.", key, bucket, presignedExpirationMinutes);

        return new GetImageResponseDTO(presignedUrl);
    }

    /**
     * Ensures the target bucket exists in MinIO.
     * <p>
     * If the bucket does not exist (404), it is created.
     * If it already exists or is inaccessible (403/409), logs a warning and skips creation.
     */
    private void ensureBucketExists() {
        String bucket = properties.getBucket();

        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.warn("Bucket '{}' not found. Creating it now...", bucket);
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            } else if (e.statusCode() == 403 || e.statusCode() == 409) {
                log.warn("Bucket '{}' exists but is not accessible (status {}). Skipping creation.", bucket, e.statusCode());
            } else {
                log.error("Unexpected error checking/creating bucket '{}'", bucket, e);
                throw e;
            }
        }
    }

    /**
     * Verifies that the object (image) exists in the bucket.
     *
     * @param key the key of the image to check
     * @throws ImageStorageException if the object does not exist or is inaccessible
     */
    private void ensureObjectExists(String key) {
        String bucket = properties.getBucket();

        try {
            s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        } catch (S3Exception e) {
            log.warn("Requested image '{}' not found in bucket '{}'", key, bucket);
            throw new ImageStorageException("Requested image does not exist", e);
        }
    }

    /**
     * Validates the uploaded file to ensure it's a non-empty image.
     *
     * @param file the uploaded file to validate
     * @throws ImageStorageException if the file is empty or not an image
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty() || file.getSize() <= 0) {
            log.warn("Uploaded file is empty.");
            throw new ImageStorageException("Uploaded file is empty.");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            log.warn("Invalid file type: {}. Only images are allowed.", file.getContentType());
            throw new ImageStorageException("Only image files are allowed.");
        }
    }

    /**
     * Generates a unique key for the uploaded image using UUID and original filename.
     *
     * @param file the image file to generate a key for
     * @return a unique key string used to store the image
     */
    private String generateUniqueKey(MultipartFile file) {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "File name is null")
            .replaceAll("\\s+", "_");
        String uuid = java.util.UUID.randomUUID().toString();
        String key = uuid + "_" + originalFilename;
        log.debug("Generated unique key for upload: {}", key);
        return key;
    }
}
