package com.gugugaga.jsmedicine.module.auth.admin.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gugugaga.jsmedicine.common.config.OperationAudit;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.auth.admin.dto.CurrentAdminResponse;
import com.gugugaga.jsmedicine.module.auth.admin.dto.LoginRequest;
import com.gugugaga.jsmedicine.module.auth.admin.dto.LoginResponse;
import com.gugugaga.jsmedicine.module.auth.admin.entity.AdminSession;
import com.gugugaga.jsmedicine.module.system.entity.SysAdmin;
import com.gugugaga.jsmedicine.module.system.mapper.SysAdminMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final DaoAuthenticationProvider daoAuthenticationProvider;
    private final AuthTokenService authTokenService;
    private final AdminAuthorizationService adminAuthorizationService;
    private final SysAdminMapper sysAdminMapper;
    private final CurrentAdminResolver currentAdminResolver;

    public AuthService(
            DaoAuthenticationProvider daoAuthenticationProvider,
            AuthTokenService authTokenService,
            AdminAuthorizationService adminAuthorizationService,
            SysAdminMapper sysAdminMapper,
            CurrentAdminResolver currentAdminResolver
    ) {
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.authTokenService = authTokenService;
        this.adminAuthorizationService = adminAuthorizationService;
        this.sysAdminMapper = sysAdminMapper;
        this.currentAdminResolver = currentAdminResolver;
    }

    @Transactional(rollbackFor = Exception.class)
    @OperationAudit(
            targetType = "sys_admin_login",
            targetId = "#result.admin().id()",
            afterStatus = "1",
            comment = "'login success'"
    )
    public LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest) {
        Authentication authentication;
        try {
            authentication = daoAuthenticationProvider.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())
            );
        } catch (DisabledException exception) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Administrator account is disabled");
        } catch (BadCredentialsException exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Username or password is incorrect");
        }

        AdminSecurityPrincipal principal = (AdminSecurityPrincipal) authentication.getPrincipal();
        AdminAuthorizationInfo authorizationInfo = adminAuthorizationService.loadByAdminId(principal.getId());
        if (authorizationInfo == null || authorizationInfo.admin().getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Administrator account is disabled");
        }

        LocalDateTime now = LocalDateTime.now();
        sysAdminMapper.update(null, new LambdaUpdateWrapper<SysAdmin>()
                .eq(SysAdmin::getId, authorizationInfo.admin().getId())
                .set(SysAdmin::getLastLoginAt, now)
                .set(SysAdmin::getLastLoginIp, resolveClientIp(httpServletRequest)));

        AdminSession session = new AdminSession(
                authorizationInfo.admin().getId(),
                authorizationInfo.admin().getUsername(),
                authorizationInfo.admin().getRealName(),
                authorizationInfo.roleCodes(),
                authorizationInfo.permissionCodes(),
                now,
                now.plusSeconds(authTokenService.tokenTtlSeconds())
        );
        AuthTokenService.TokenIssueResult issueResult = authTokenService.issue(session);
        return new LoginResponse(
                "Bearer",
                issueResult.token(),
                issueResult.expiresInSeconds(),
                new LoginResponse.AdminProfile(
                        authorizationInfo.admin().getId(),
                        authorizationInfo.admin().getUsername(),
                        authorizationInfo.admin().getRealName(),
                        now
                ),
                authorizationInfo.permissionCodes()
        );
    }

    public void logout(String authorizationHeader) {
        String token = currentAdminResolver.resolveRawToken(authorizationHeader)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Missing bearer token"));
        authTokenService.delete(token);
        SecurityContextHolder.clearContext();
    }

    public CurrentAdminResponse currentAdmin() {
        AdminAuthorizationInfo authorizationInfo = currentAdminResolver.requireCurrentAdmin();
        return new CurrentAdminResponse(
                authorizationInfo.admin().getId(),
                authorizationInfo.admin().getUsername(),
                authorizationInfo.admin().getRealName(),
                authorizationInfo.roleCodes(),
                authorizationInfo.permissionCodes()
        );
    }

    public boolean validateCurrentToken() {
        return currentAdminResolver.currentSession().isPresent();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

