package com.gugugaga.jsmedicine.module.auth.service;

import com.gugugaga.jsmedicine.module.auth.entity.AdminSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthProperties authProperties;

    public AuthTokenService(RedisTemplate<String, Object> redisTemplate, AuthProperties authProperties) {
        this.redisTemplate = redisTemplate;
        this.authProperties = authProperties;
    }

    public TokenIssueResult issue(AdminSession session) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(buildKey(token), session, Duration.ofSeconds(authProperties.getTokenTtlSeconds()));
        return new TokenIssueResult(token, authProperties.getTokenTtlSeconds());
    }

    public long tokenTtlSeconds() {
        return authProperties.getTokenTtlSeconds();
    }

    public Optional<AdminSession> getSession(String token) {
        Object value = redisTemplate.opsForValue().get(buildKey(token));
        if (value instanceof AdminSession session) {
            return Optional.of(session);
        }
        return Optional.empty();
    }

    public void delete(String token) {
        redisTemplate.delete(buildKey(token));
    }

    private String buildKey(String token) {
        return authProperties.getTokenPrefix() + token;
    }

    public record TokenIssueResult(String token, long expiresInSeconds) {
    }
}
