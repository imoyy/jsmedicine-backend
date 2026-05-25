package com.gugugaga.jsmedicine.module.auth.app.service;

import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppUserTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AppAuthProperties appAuthProperties;

    public AppUserTokenService(RedisTemplate<String, Object> redisTemplate, AppAuthProperties appAuthProperties) {
        this.redisTemplate = redisTemplate;
        this.appAuthProperties = appAuthProperties;
    }

    public TokenIssueResult issue(AppUserSession session) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(buildKey(token), session, Duration.ofSeconds(appAuthProperties.getTokenTtlSeconds()));
        return new TokenIssueResult(token, appAuthProperties.getTokenTtlSeconds());
    }

    public Optional<AppUserSession> getSession(String token) {
        Object value = redisTemplate.opsForValue().get(buildKey(token));
        if (value instanceof AppUserSession session) {
            return Optional.of(session);
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

    public record TokenIssueResult(String token, long expiresInSeconds) {
    }
}
