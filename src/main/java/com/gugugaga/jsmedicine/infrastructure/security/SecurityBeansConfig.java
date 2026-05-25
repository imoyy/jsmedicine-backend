package com.gugugaga.jsmedicine.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gugugaga.jsmedicine.module.auth.service.AdminUserDetailsService;
import com.gugugaga.jsmedicine.module.auth.service.AuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityBeansConfig {

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            AdminUserDetailsService adminUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(adminUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper
    ) {
        GenericJackson2JsonRedisSerializer jsonRedisSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        return redisTemplate;
    }
}
