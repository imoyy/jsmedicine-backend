package com.gugugaga.jsmedicine.module.auth.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.auth.app.dto.CurrentAppUserResponse;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginResponse;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
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
public class AppAuthService {

    private final DaoAuthenticationProvider appUserAuthenticationProvider;
    private final AppUserMapper appUserMapper;
    private final AppUserTokenService appUserTokenService;
    private final CurrentAppUserResolver currentAppUserResolver;
    private final StudentMapper studentMapper;

    public AppAuthService(
            DaoAuthenticationProvider appUserAuthenticationProvider,
            AppUserMapper appUserMapper,
            AppUserTokenService appUserTokenService,
            CurrentAppUserResolver currentAppUserResolver,
            StudentMapper studentMapper
    ) {
        this.appUserAuthenticationProvider = appUserAuthenticationProvider;
        this.appUserMapper = appUserMapper;
        this.appUserTokenService = appUserTokenService;
        this.currentAppUserResolver = currentAppUserResolver;
        this.studentMapper = studentMapper;
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

    public void logout(String authorizationHeader) {
        String token = currentAppUserResolver.resolveRawToken(authorizationHeader)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Missing bearer token"));
        appUserTokenService.delete(token);
        SecurityContextHolder.clearContext();
    }

    public boolean validateCurrentToken() {
        return currentAppUserResolver.currentSession().isPresent();
    }

    public CurrentAppUserResponse currentUser() {
        AppUserSession session = currentAppUserResolver.requireCurrentUser();
        AppUser appUser = appUserMapper.selectById(session.userId());
        if (appUser == null || appUser.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "App user account does not exist");
        }
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, appUser.getId())
                .eq(Student::getDeleted, 0)
                .last("LIMIT 1"));
        return new CurrentAppUserResponse(
                appUser.getId(),
                appUser.getUsername(),
                appUser.getNickname(),
                appUser.getAvatarUrl(),
                appUser.getMobile(),
                appUser.getEmail(),
                appUser.getProfileCompleted(),
                student == null ? null : student.getId(),
                student == null ? null : student.getCertificationStatus()
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
