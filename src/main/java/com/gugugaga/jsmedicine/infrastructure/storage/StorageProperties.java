package com.gugugaga.jsmedicine.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String provider = "minio";
    private String endpoint = "http://127.0.0.1:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin123";
    private String publicBaseUrl;
    private String presignedUploadBaseUrl;
    private boolean autoCreateBucket = true;
    private Avatar avatar = new Avatar();
    private Cover cover = new Cover();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String getPresignedUploadBaseUrl() {
        return presignedUploadBaseUrl;
    }

    public void setPresignedUploadBaseUrl(String presignedUploadBaseUrl) {
        this.presignedUploadBaseUrl = presignedUploadBaseUrl;
    }

    public boolean isAutoCreateBucket() {
        return autoCreateBucket;
    }

    public void setAutoCreateBucket(boolean autoCreateBucket) {
        this.autoCreateBucket = autoCreateBucket;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }

    public Cover getCover() {
        return cover;
    }

    public void setCover(Cover cover) {
        this.cover = cover;
    }

    public static class Avatar {
        private String bucketName = "public";
        private String objectPrefix = "app-users";
        private long maxFileSizeBytes = 5L * 1024 * 1024;
        private long uploadUrlTtlSeconds = 900;
        private List<String> allowedContentTypes = new ArrayList<>(List.of(
                "image/jpeg",
                "image/png",
                "image/webp"
        ));

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getObjectPrefix() {
            return objectPrefix;
        }

        public void setObjectPrefix(String objectPrefix) {
            this.objectPrefix = objectPrefix;
        }

        public long getMaxFileSizeBytes() {
            return maxFileSizeBytes;
        }

        public void setMaxFileSizeBytes(long maxFileSizeBytes) {
            this.maxFileSizeBytes = maxFileSizeBytes;
        }

        public long getUploadUrlTtlSeconds() {
            return uploadUrlTtlSeconds;
        }

        public void setUploadUrlTtlSeconds(long uploadUrlTtlSeconds) {
            this.uploadUrlTtlSeconds = uploadUrlTtlSeconds;
        }

        public List<String> getAllowedContentTypes() {
            return allowedContentTypes;
        }

        public void setAllowedContentTypes(List<String> allowedContentTypes) {
            this.allowedContentTypes = allowedContentTypes == null ? new ArrayList<>() : new ArrayList<>(allowedContentTypes);
        }
    }

    public static class Cover {
        private String bucketName = "public";
        private String objectPrefix = "admin/covers";
        private long maxFileSizeBytes = 5L * 1024 * 1024;
        private long uploadUrlTtlSeconds = 900;
        private List<String> allowedContentTypes = new ArrayList<>(List.of(
                "image/jpeg",
                "image/png",
                "image/webp"
        ));

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getObjectPrefix() {
            return objectPrefix;
        }

        public void setObjectPrefix(String objectPrefix) {
            this.objectPrefix = objectPrefix;
        }

        public long getMaxFileSizeBytes() {
            return maxFileSizeBytes;
        }

        public void setMaxFileSizeBytes(long maxFileSizeBytes) {
            this.maxFileSizeBytes = maxFileSizeBytes;
        }

        public long getUploadUrlTtlSeconds() {
            return uploadUrlTtlSeconds;
        }

        public void setUploadUrlTtlSeconds(long uploadUrlTtlSeconds) {
            this.uploadUrlTtlSeconds = uploadUrlTtlSeconds;
        }

        public List<String> getAllowedContentTypes() {
            return allowedContentTypes;
        }

        public void setAllowedContentTypes(List<String> allowedContentTypes) {
            this.allowedContentTypes = allowedContentTypes == null ? new ArrayList<>() : new ArrayList<>(allowedContentTypes);
        }
    }
}
