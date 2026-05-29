package com.gugugaga.jsmedicine.infrastructure.storage;

import java.time.Duration;

public interface StorageClient {

    StorageUploadUrl createPresignedUploadUrl(String bucketName, String objectKey, Duration ttl);

    StorageObjectStat statObject(String bucketName, String objectKey);

    StorageObjectStream getObject(String bucketName, String objectKey);
}
