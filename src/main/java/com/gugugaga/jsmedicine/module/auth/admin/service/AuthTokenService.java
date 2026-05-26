package com.gugugaga.jsmedicine.module.auth.admin.service;

import com.gugugaga.jsmedicine.module.auth.admin.entity.AdminSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AuthProperties authProperties;

    public AuthTokenService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, AuthProperties authProperties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.authProperties = authProperties;
    }

    public TokenIssueResult issue(AdminSession session) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(
                buildKey(token),
                serialize(session),
                Duration.ofSeconds(authProperties.getTokenTtlSeconds())
        );
        return new TokenIssueResult(token, authProperties.getTokenTtlSeconds());
    }

    public long tokenTtlSeconds() {
        return authProperties.getTokenTtlSeconds();
    }

    public Optional<AdminSession> getSession(String token) {
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
        return authProperties.getTokenPrefix() + token;
    }

    private String serialize(AdminSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize admin session", exception);
        }
    }

    private Optional<AdminSession> deserialize(String json) {
        try {
            return Optional.of(objectMapper.readValue(json, AdminSession.class));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    public record TokenIssueResult(String token, long expiresInSeconds) {
    }
}
