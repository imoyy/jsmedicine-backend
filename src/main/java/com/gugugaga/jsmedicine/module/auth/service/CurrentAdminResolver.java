package com.gugugaga.jsmedicine.module.auth.service;

import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.auth.entity.AdminSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentAdminResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthTokenService authTokenService;
    private final AdminAuthorizationService adminAuthorizationService;

    public CurrentAdminResolver(AuthTokenService authTokenService, AdminAuthorizationService adminAuthorizationService) {
        this.authTokenService = authTokenService;
        this.adminAuthorizationService = adminAuthorizationService;
    }

    public Optional<AdminSession> currentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getCredentials() instanceof String token)) {
            return Optional.empty();
        }
        return authTokenService.getSession(token);
    }

    public AdminAuthorizationInfo requireCurrentAdmin() {
        AdminSession session = currentSession()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Token is invalid or expired"));
        AdminAuthorizationInfo authorizationInfo = adminAuthorizationService.loadByAdminId(session.adminId());
        if (authorizationInfo == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Administrator account does not exist");
        }
        return authorizationInfo;
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
