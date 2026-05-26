package com.gugugaga.jsmedicine.module.auth.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.common.enums.UserAuthProvider;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.auth.app.dto.CurrentAppUserResponse;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginResponse;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppSmsLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppWechatLoginRequest;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AppAuthService {

    private static final int SMS_CODE_BOUND = 1_000_000;
    private static final String SMS_CODE_FORMAT = "%06d";

    private final DaoAuthenticationProvider appUserAuthenticationProvider;
    private final AppUserMapper appUserMapper;
    private final AppUserTokenService appUserTokenService;
    private final CurrentAppUserResolver currentAppUserResolver;
    private final StudentMapper studentMapper;
    private final AppAuthProperties appAuthProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AliyunSmsCodeSender aliyunSmsCodeSender;
    private final WechatMiniappClient wechatMiniappClient;
    private final SecureRandom secureRandom = new SecureRandom();

    public AppAuthService(
            DaoAuthenticationProvider appUserAuthenticationProvider,
            AppUserMapper appUserMapper,
            AppUserTokenService appUserTokenService,
            CurrentAppUserResolver currentAppUserResolver,
            StudentMapper studentMapper,
            AppAuthProperties appAuthProperties,
            RedisTemplate<String, Object> redisTemplate,
            AliyunSmsCodeSender aliyunSmsCodeSender,
            WechatMiniappClient wechatMiniappClient
    ) {
        this.appUserAuthenticationProvider = appUserAuthenticationProvider;
        this.appUserMapper = appUserMapper;
        this.appUserTokenService = appUserTokenService;
        this.currentAppUserResolver = currentAppUserResolver;
        this.studentMapper = studentMapper;
        this.appAuthProperties = appAuthProperties;
        this.redisTemplate = redisTemplate;
        this.aliyunSmsCodeSender = aliyunSmsCodeSender;
        this.wechatMiniappClient = wechatMiniappClient;
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

    public void sendSmsCode(String mobile) {
        String code = resolveSmsCode();
        redisTemplate.opsForValue().set(
                buildSmsCodeKey(mobile),
                code,
                Duration.ofSeconds(appAuthProperties.getSms().getCodeTtlSeconds())
        );
        if (!appAuthProperties.getSms().isMockEnabled()) {
            aliyunSmsCodeSender.sendCode(mobile, code);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public AppLoginResponse loginBySms(AppSmsLoginRequest request, HttpServletRequest httpServletRequest) {
        Object cachedCode = redisTemplate.opsForValue().get(buildSmsCodeKey(request.mobile()));
        if (!(cachedCode instanceof String code) || !code.equals(request.code())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Sms verification code is invalid");
        }
        redisTemplate.delete(buildSmsCodeKey(request.mobile()));
        AppUser appUser = findUserByMobile(request.mobile())
                .orElseGet(() -> createMobileUser(request.mobile()));
        return issueLoginResponse(appUser, httpServletRequest);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppLoginResponse loginByWechat(AppWechatLoginRequest request, HttpServletRequest httpServletRequest) {
        WechatMiniappClient.WechatSession session = wechatMiniappClient.codeToSession(request.code());
        AppUser appUser = findUserByWechatOpenId(session.openId())
                .orElseGet(() -> createWechatUser(session, request));
        boolean shouldUpdateProfile = false;
        if (request.nickname() != null && !request.nickname().isBlank()
                && (appUser.getNickname() == null || appUser.getNickname().isBlank())) {
            appUser.setNickname(request.nickname());
            shouldUpdateProfile = true;
        }
        if (request.avatarUrl() != null && !request.avatarUrl().isBlank()
                && (appUser.getAvatarUrl() == null || appUser.getAvatarUrl().isBlank())) {
            appUser.setAvatarUrl(request.avatarUrl());
            shouldUpdateProfile = true;
        }
        if (session.unionId() != null && !session.unionId().isBlank()
                && (appUser.getWechatUnionId() == null || appUser.getWechatUnionId().isBlank())) {
            appUser.setWechatUnionId(session.unionId());
            shouldUpdateProfile = true;
        }
        if (shouldUpdateProfile) {
            appUserMapper.updateById(appUser);
        }
        return issueLoginResponse(appUser, httpServletRequest);
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

    private AppLoginResponse issueLoginResponse(AppUser appUser, HttpServletRequest httpServletRequest) {
        if (appUser == null || appUser.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "App user account is disabled");
        }
        LocalDateTime now = LocalDateTime.now();
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, appUser.getId())
                .set(AppUser::getLastLoginAt, now)
                .set(AppUser::getLastLoginIp, resolveClientIp(httpServletRequest)));
        AppUser refreshed = appUserMapper.selectById(appUser.getId());
        AppUserSession session = new AppUserSession(
                refreshed.getId(),
                refreshed.getUsername(),
                refreshed.getNickname(),
                now,
                now.plusSeconds(appUserTokenService.tokenTtlSeconds())
        );
        AppUserTokenService.TokenIssueResult issueResult = appUserTokenService.issue(session);
        return new AppLoginResponse(
                "Bearer",
                issueResult.token(),
                issueResult.expiresInSeconds(),
                new AppLoginResponse.UserProfile(
                        refreshed.getId(),
                        refreshed.getUsername(),
                        refreshed.getNickname(),
                        refreshed.getAvatarUrl(),
                        now
                )
        );
    }

    private Optional<AppUser> findUserByMobile(String mobile) {
        return Optional.ofNullable(appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getMobile, mobile)
                .eq(AppUser::getDeleted, 0)
                .last("LIMIT 1")));
    }

    private Optional<AppUser> findUserByWechatOpenId(String openId) {
        return Optional.ofNullable(appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getWechatOpenId, openId)
                .eq(AppUser::getDeleted, 0)
                .last("LIMIT 1")));
    }

    private AppUser createMobileUser(String mobile) {
        LocalDateTime now = LocalDateTime.now();
        AppUser appUser = new AppUser();
        appUser.setUsername("u" + mobile);
        appUser.setMobile(mobile);
        appUser.setNickname(maskMobile(mobile));
        appUser.setAuthProvider(UserAuthProvider.MOBILE_SMS);
        appUser.setGender(Gender.UNKNOWN);
        appUser.setStatus(EnabledStatus.ENABLED);
        appUser.setRegisteredAt(now);
        appUser.setProfileCompleted(false);
        appUser.setDeleted(0);
        appUserMapper.insert(appUser);
        return appUser;
    }

    private AppUser createWechatUser(WechatMiniappClient.WechatSession session, AppWechatLoginRequest request) {
        LocalDateTime now = LocalDateTime.now();
        AppUser appUser = new AppUser();
        appUser.setUsername("wx_" + session.openId());
        appUser.setNickname(resolveWechatNickname(request, session));
        appUser.setAvatarUrl(request.avatarUrl());
        appUser.setAuthProvider(UserAuthProvider.WECHAT_MINIAPP);
        appUser.setWechatOpenId(session.openId());
        appUser.setWechatUnionId(session.unionId());
        appUser.setGender(Gender.UNKNOWN);
        appUser.setStatus(EnabledStatus.ENABLED);
        appUser.setRegisteredAt(now);
        appUser.setProfileCompleted(false);
        appUser.setDeleted(0);
        appUserMapper.insert(appUser);
        return appUser;
    }

    private String resolveSmsCode() {
        String mockCode = appAuthProperties.getSms().getMockCode();
        if (appAuthProperties.getSms().isMockEnabled() && mockCode != null && !mockCode.isBlank()) {
            return mockCode;
        }
        return String.format(SMS_CODE_FORMAT, secureRandom.nextInt(SMS_CODE_BOUND));
    }

    private String buildSmsCodeKey(String mobile) {
        return appAuthProperties.getSms().getCodePrefix() + mobile;
    }

    private String maskMobile(String mobile) {
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    private String resolveWechatNickname(AppWechatLoginRequest request, WechatMiniappClient.WechatSession session) {
        if (request.nickname() != null && !request.nickname().isBlank()) {
            return request.nickname();
        }
        return "wx_" + session.openId().substring(Math.max(0, session.openId().length() - 8));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
