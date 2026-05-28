package com.gugugaga.jsmedicine.module.auth.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class WechatBindTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AppAuthProperties appAuthProperties;

    public WechatBindTokenService(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper,
            AppAuthProperties appAuthProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.appAuthProperties = appAuthProperties;
    }

    public TokenIssueResult issue(PendingWechatBinding binding) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(
                buildKey(token),
                serialize(binding),
                Duration.ofSeconds(appAuthProperties.getWechat().getBindTokenTtlSeconds())
        );
        return new TokenIssueResult(token, appAuthProperties.getWechat().getBindTokenTtlSeconds());
    }

    public Optional<PendingWechatBinding> get(String token) {
        Object value = redisTemplate.opsForValue().get(buildKey(token));
        if (value instanceof String json) {
            return deserialize(json);
        }
        return Optional.empty();
    }

    public void delete(String token) {
        redisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {
        return appAuthProperties.getWechat().getBindTokenPrefix() + token;
    }

    private String serialize(PendingWechatBinding binding) {
        try {
            return objectMapper.writeValueAsString(binding);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize pending wechat binding", exception);
        }
    }

    private Optional<PendingWechatBinding> deserialize(String json) {
        try {
            return Optional.of(objectMapper.readValue(json, PendingWechatBinding.class));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    public record PendingWechatBinding(
            String openId,
            String unionId,
            String nickname,
            String avatarUrl
    ) {
    }

    public record TokenIssueResult(String token, long expiresInSeconds) {
    }
}
