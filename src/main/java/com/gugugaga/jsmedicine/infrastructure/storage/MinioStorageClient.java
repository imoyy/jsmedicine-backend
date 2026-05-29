package com.gugugaga.jsmedicine.infrastructure.storage;

import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MinioStorageClient implements StorageClient {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageClient.class);
    private static final int MAX_PRESIGNED_EXPIRY_SECONDS = 7 * 24 * 60 * 60;

    private final StorageProperties storageProperties;
    private final MinioClient minioClient;
    private final Set<String> readyBuckets = ConcurrentHashMap.newKeySet();

    public MinioStorageClient(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
        if (!"minio".equalsIgnoreCase(storageProperties.getProvider())) {
            throw new IllegalStateException("Unsupported storage provider: " + storageProperties.getProvider());
        }
        this.minioClient = MinioClient.builder()
                .endpoint(storageProperties.getEndpoint())
                .credentials(storageProperties.getAccessKey(), storageProperties.getSecretKey())
                .build();
    }

    @Override
    public StorageUploadUrl createPresignedUploadUrl(String bucketName, String objectKey, Duration ttl) {
        ensureBucketExists(bucketName);
        int expirySeconds = normalizeExpirySeconds(ttl);
        try {
            String uploadUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(expirySeconds)
                    .build());
            return new StorageUploadUrl("PUT", uploadUrl, LocalDateTime.now().plusSeconds(expirySeconds));
        } catch (Exception exception) {
            log.error("Failed to create presigned upload url, bucket={}, objectKey={}", bucketName, objectKey, exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to create upload url");
        }
    }

    @Override
    public StorageObjectStat statObject(String bucketName, String objectKey) {
        ensureBucketExists(bucketName);
        try {
            var response = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
            String contentType = response.contentType();
            return new StorageObjectStat(contentType == null ? "" : contentType, response.size());
        } catch (ErrorResponseException exception) {
            throw translateObjectException(bucketName, objectKey, exception, "Failed to query uploaded file");
        } catch (Exception exception) {
            log.error("Failed to stat object, bucket={}, objectKey={}", bucketName, objectKey, exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to query uploaded file");
        }
    }

    @Override
    public StorageObjectStream getObject(String bucketName, String objectKey) {
        ensureBucketExists(bucketName);
        try {
            var stat = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
            var response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
            String contentType = stat.contentType();
            return new StorageObjectStream(response, contentType == null ? "" : contentType, stat.size());
        } catch (ErrorResponseException exception) {
            throw translateObjectException(bucketName, objectKey, exception, "Failed to read file");
        } catch (Exception exception) {
            log.error("Failed to get object, bucket={}, objectKey={}", bucketName, objectKey, exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to read file");
        }
    }

    private void ensureBucketExists(String bucketName) {
        if (!storageProperties.isAutoCreateBucket() || readyBuckets.contains(bucketName)) {
            return;
        }
        synchronized (readyBuckets) {
            if (readyBuckets.contains(bucketName)) {
                return;
            }
            try {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());
                }
                readyBuckets.add(bucketName);
            } catch (ErrorResponseException exception) {
                String code = exception.errorResponse() == null ? "" : exception.errorResponse().code();
                if ("BucketAlreadyOwnedByYou".equalsIgnoreCase(code) || "BucketAlreadyExists".equalsIgnoreCase(code)) {
                    readyBuckets.add(bucketName);
                    return;
                }
                log.error("Failed to ensure bucket exists, bucket={}", bucketName, exception);
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to initialize object storage bucket");
            } catch (Exception exception) {
                log.error("Failed to ensure bucket exists, bucket={}", bucketName, exception);
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to initialize object storage bucket");
            }
        }
    }

    private BusinessException translateObjectException(
            String bucketName,
            String objectKey,
            ErrorResponseException exception,
            String fallbackMessage
    ) {
        String code = exception.errorResponse() == null ? "" : exception.errorResponse().code();
        if ("NoSuchKey".equalsIgnoreCase(code) || "NoSuchObject".equalsIgnoreCase(code)) {
            return new BusinessException(ErrorCode.NOT_FOUND, "Uploaded file does not exist");
        }
        if ("NoSuchBucket".equalsIgnoreCase(code)) {
            return new BusinessException(ErrorCode.INTERNAL_ERROR, "Object storage bucket does not exist");
        }
        log.error("Storage request failed, bucket={}, objectKey={}, code={}",
                bucketName, objectKey, code, exception);
        return new BusinessException(ErrorCode.INTERNAL_ERROR, fallbackMessage);
    }

    private int normalizeExpirySeconds(Duration ttl) {
        long seconds = ttl == null ? 0 : ttl.getSeconds();
        if (seconds < 1) {
            seconds = storageProperties.getAvatar().getUploadUrlTtlSeconds();
        }
        return (int) Math.min(seconds, MAX_PRESIGNED_EXPIRY_SECONDS);
    }
}
