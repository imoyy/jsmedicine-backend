package com.gugugaga.jsmedicine.module.auth.app.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginResponse;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AppAuthService {

    private final DaoAuthenticationProvider appUserAuthenticationProvider;
    private final AppUserMapper appUserMapper;
    private final AppUserTokenService appUserTokenService;

    public AppAuthService(
            DaoAuthenticationProvider appUserAuthenticationProvider,
            AppUserMapper appUserMapper,
            AppUserTokenService appUserTokenService
    ) {
        this.appUserAuthenticationProvider = appUserAuthenticationProvider;
        this.appUserMapper = appUserMapper;
        this.appUserTokenService = appUserTokenService;
    }

    @Transactional(rollbackFor = Exception.class)
    public AppLoginResponse login(AppLoginRequest request, HttpServletRequest httpServletRequest) {
        Authentication authentication;
        try {
            authentication = appUserAuthenticationProvider.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())
            );
        } catch (DisabledException exception) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "App user account is disabled");
        } catch (BadCredentialsException exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Username or password is incorrect");
        }

        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        AppUser appUser = appUserMapper.selectById(principal.getId());
        if (appUser == null || appUser.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "App user account is disabled");
        }
        LocalDateTime now = LocalDateTime.now();
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, appUser.getId())
                .set(AppUser::getLastLoginAt, now)
                .set(AppUser::getLastLoginIp, resolveClientIp(httpServletRequest)));
        AppUserSession session = new AppUserSession(
                appUser.getId(),
                appUser.getUsername(),
                appUser.getNickname(),
                now,
                now.plusSeconds(appUserTokenService.tokenTtlSeconds())
        );
        AppUserTokenService.TokenIssueResult issueResult = appUserTokenService.issue(session);
        return new AppLoginResponse(
                "Bearer",
                issueResult.token(),
                issueResult.expiresInSeconds(),
                new AppLoginResponse.UserProfile(
                        appUser.getId(),
                        appUser.getUsername(),
                        appUser.getNickname(),
                        appUser.getAvatarUrl(),
                        now
                )
        );
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
