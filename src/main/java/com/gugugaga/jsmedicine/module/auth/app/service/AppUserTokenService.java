package com.gugugaga.jsmedicine.module.auth.app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppUserTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AppAuthProperties appAuthProperties;

    public AppUserTokenService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, AppAuthProperties appAuthProperties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.appAuthProperties = appAuthProperties;
    }

    public TokenIssueResult issue(AppUserSession session) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(
                buildKey(token),
                serialize(session),
                Duration.ofSeconds(appAuthProperties.getTokenTtlSeconds())
        );
        return new TokenIssueResult(token, appAuthProperties.getTokenTtlSeconds());
    }

    public Optional<AppUserSession> getSession(String token) {
        Object value = redisTemplate.opsForValue().get(buildKey(token));
        if (value instanceof String json) {
            return deserialize(json);
        }
        return Optional.empty();
    }

    public void delete(String token) {
        redisTemplate.delete(buildKey(token));
    }

    public long tokenTtlSeconds() {
        return appAuthProperties.getTokenTtlSeconds();
    }

    private String buildKey(String token) {
        return appAuthProperties.getTokenPrefix() + token;
    }

    private String serialize(AppUserSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize app user session", exception);
        }
    }

    private Optional<AppUserSession> deserialize(String json) {
        try {
            return Optional.of(objectMapper.readValue(json, AppUserSession.class));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    public record TokenIssueResult(String token, long expiresInSeconds) {
    }
}
