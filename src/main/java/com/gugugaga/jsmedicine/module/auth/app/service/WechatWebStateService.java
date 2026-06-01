package com.gugugaga.jsmedicine.module.auth.app.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class WechatWebStateService {

    private static final String STATE_MARKER = "1";

    private final RedisTemplate<String, Object> redisTemplate;
    private final AppAuthProperties appAuthProperties;

    public WechatWebStateService(RedisTemplate<String, Object> redisTemplate, AppAuthProperties appAuthProperties) {
        this.redisTemplate = redisTemplate;
        this.appAuthProperties = appAuthProperties;
    }

    public TokenIssueResult issue() {
        String state = UUID.randomUUID().toString().replace("-", "");
        long ttlSeconds = appAuthProperties.getWechatWeb().getStateTtlSeconds();
        redisTemplate.opsForValue().set(
                buildKey(state),
                STATE_MARKER,
                Duration.ofSeconds(ttlSeconds)
        );
        return new TokenIssueResult(state, ttlSeconds);
    }

    public boolean consume(String state) {
        String key = buildKey(state);
        Object cachedValue = redisTemplate.opsForValue().get(key);
        if (!(cachedValue instanceof String)) {
            return false;
        }
        redisTemplate.delete(key);
        return true;
    }

    private String buildKey(String state) {
        return appAuthProperties.getWechatWeb().getStatePrefix() + state;
    }

    public record TokenIssueResult(String state, long expiresInSeconds) {
    }
}
