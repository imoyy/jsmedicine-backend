package com.gugugaga.jsmedicine.module.auth.app.service;

import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentAppUserResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AppUserTokenService appUserTokenService;

    public CurrentAppUserResolver(AppUserTokenService appUserTokenService) {
        this.appUserTokenService = appUserTokenService;
    }

    public Optional<AppUserSession> currentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal)) {
            return Optional.empty();
        }
        if (!(authentication.getCredentials() instanceof String token)) {
            return Optional.empty();
        }
        return appUserTokenService.getSession(token);
    }

    public AppUserSession requireCurrentUser() {
        return currentSession()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Token is invalid or expired"));
    }

    public Optional<String> resolveRawToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return token.isBlank() ? Optional.empty() : Optional.of(token);
    }
}
