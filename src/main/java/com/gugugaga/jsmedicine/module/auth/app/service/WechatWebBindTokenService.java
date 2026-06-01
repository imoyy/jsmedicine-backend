package com.gugugaga.jsmedicine.module.auth.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class WechatWebBindTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AppAuthProperties appAuthProperties;

    public WechatWebBindTokenService(
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper,
            AppAuthProperties appAuthProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.appAuthProperties = appAuthProperties;
    }

    public TokenIssueResult issue(PendingWechatWebBinding binding) {
        String token = UUID.randomUUID().toString().replace("-", "");
        long ttlSeconds = appAuthProperties.getWechatWeb().getBindTokenTtlSeconds();
        redisTemplate.opsForValue().set(
                buildKey(token),
                serialize(binding),
                Duration.ofSeconds(ttlSeconds)
        );
        return new TokenIssueResult(token, ttlSeconds);
    }

    public Optional<PendingWechatWebBinding> get(String token) {
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
        return appAuthProperties.getWechatWeb().getBindTokenPrefix() + token;
    }

    private String serialize(PendingWechatWebBinding binding) {
        try {
            return objectMapper.writeValueAsString(binding);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize pending website wechat binding", exception);
        }
    }

    private Optional<PendingWechatWebBinding> deserialize(String json) {
        try {
            return Optional.of(objectMapper.readValue(json, PendingWechatWebBinding.class));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    public record PendingWechatWebBinding(
            String openId,
            String unionId
    ) {
    }

    public record TokenIssueResult(String token, long expiresInSeconds) {
    }
}
