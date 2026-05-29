package com.gugugaga.jsmedicine.infrastructure.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    @Bean
    public StorageClient storageClient(StorageProperties storageProperties) {
        return new MinioStorageClient(storageProperties);
    }
}
