package com.gugugaga.jsmedicine.module.auth.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityStatus;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.common.enums.UserAuthProvider;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.infrastructure.storage.service.AppUserAvatarUrlResolver;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginResponse;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppSmsLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppWechatBindMobileRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppWechatLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppWechatLoginResponse;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppWechatWebLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppWechatWebQrConfigResponse;
import com.gugugaga.jsmedicine.module.auth.app.dto.CurrentAppUserResponse;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppCurrentExpertResponse;
import com.gugugaga.jsmedicine.module.expert.app.entity.AppExpertSession;
import com.gugugaga.jsmedicine.module.expert.app.service.CurrentAppExpertResolver;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.user.entity.AppUserIdentity;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserIdentityMapper;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppAuthService {

    private static final Logger log = LoggerFactory.getLogger(AppAuthService.class);
    private static final int SMS_CODE_BOUND = 1_000_000;
    private static final String SMS_CODE_FORMAT = "%06d";
    private static final String MOCK_WECHAT_WEB_APP_ID = "mock-wechat-web-appid";
    private static final String MOCK_WECHAT_WEB_REDIRECT_URI = "http://127.0.0.1/mock/wechat/callback";
    private static final String MOCK_WECHAT_WEB_SCOPE = "snsapi_login";

    private final DaoAuthenticationProvider appUserAuthenticationProvider;
    private final AppUserMapper appUserMapper;
    private final AppUserTokenService appUserTokenService;
    private final CurrentAppUserResolver currentAppUserResolver;
    private final CurrentAppExpertResolver currentAppExpertResolver;
    private final StudentMapper studentMapper;
    private final AppUserIdentityMapper appUserIdentityMapper;
    private final AppAuthProperties appAuthProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AliyunSmsCodeSender aliyunSmsCodeSender;
    private final WechatMiniappClient wechatMiniappClient;
    private final WechatBindTokenService wechatBindTokenService;
    private final WechatWebClient wechatWebClient;
    private final WechatWebStateService wechatWebStateService;
    private final WechatWebBindTokenService wechatWebBindTokenService;
    private final AppUserAvatarUrlResolver appUserAvatarUrlResolver;
    private final SecureRandom secureRandom = new SecureRandom();

    public AppAuthService(
            DaoAuthenticationProvider appUserAuthenticationProvider,
            AppUserMapper appUserMapper,
            AppUserTokenService appUserTokenService,
            CurrentAppUserResolver currentAppUserResolver,
            CurrentAppExpertResolver currentAppExpertResolver,
            StudentMapper studentMapper,
            AppUserIdentityMapper appUserIdentityMapper,
            AppAuthProperties appAuthProperties,
            RedisTemplate<String, Object> redisTemplate,
            AliyunSmsCodeSender aliyunSmsCodeSender,
            WechatMiniappClient wechatMiniappClient,
            WechatBindTokenService wechatBindTokenService,
            WechatWebClient wechatWebClient,
            WechatWebStateService wechatWebStateService,
            WechatWebBindTokenService wechatWebBindTokenService,
            AppUserAvatarUrlResolver appUserAvatarUrlResolver
    ) {
        this.appUserAuthenticationProvider = appUserAuthenticationProvider;
        this.appUserMapper = appUserMapper;
        this.appUserTokenService = appUserTokenService;
        this.currentAppUserResolver = currentAppUserResolver;
        this.currentAppExpertResolver = currentAppExpertResolver;
        this.studentMapper = studentMapper;
        this.appUserIdentityMapper = appUserIdentityMapper;
        this.appAuthProperties = appAuthProperties;
        this.redisTemplate = redisTemplate;
        this.aliyunSmsCodeSender = aliyunSmsCodeSender;
        this.wechatMiniappClient = wechatMiniappClient;
        this.wechatBindTokenService = wechatBindTokenService;
        this.wechatWebClient = wechatWebClient;
        this.wechatWebStateService = wechatWebStateService;
        this.wechatWebBindTokenService = wechatWebBindTokenService;
        this.appUserAvatarUrlResolver = appUserAvatarUrlResolver;
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
        return issueLoginResponse(appUser, httpServletRequest);
    }

    public void sendSmsCode(String mobile) {
        boolean mockMode = !aliyunSmsCodeSender.isConfigured();
        long ttlSeconds = appAuthProperties.getSms().getCodeTtlSeconds();
        if (mockMode) {
            String code = resolveSmsCode();
            redisTemplate.opsForValue().set(
                    buildSmsCodeKey(mobile),
                    code,
                    Duration.ofSeconds(ttlSeconds)
            );
            log.info("App sms verification code generated in mock mode because aliyun sms is not configured mobile={} ttlSeconds={}",
                    maskMobile(mobile),
                    ttlSeconds);
            return;
        }
        String code = aliyunSmsCodeSender.sendCode(mobile, ttlSeconds);
        redisTemplate.opsForValue().set(
                buildSmsCodeKey(mobile),
                code,
                Duration.ofSeconds(ttlSeconds)
        );
        log.info("App sms verification code sent mobile={} provider=aliyun-dypns ttlSeconds={}",
                maskMobile(mobile),
                ttlSeconds);
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

    public AppWechatWebQrConfigResponse getWechatWebQrConfig() {
        WechatWebStateService.TokenIssueResult stateIssueResult = wechatWebStateService.issue();
        AppAuthProperties.WechatWeb wechatWeb = appAuthProperties.getWechatWeb();
        if (!wechatWeb.isMockEnabled()) {
            wechatWebClient.validateConfig();
        }
        return new AppWechatWebQrConfigResponse(
                hasText(wechatWeb.getAppId()) ? wechatWeb.getAppId() : MOCK_WECHAT_WEB_APP_ID,
                hasText(wechatWeb.getRedirectUri()) ? wechatWeb.getRedirectUri() : MOCK_WECHAT_WEB_REDIRECT_URI,
                hasText(wechatWeb.getScope()) ? wechatWeb.getScope() : MOCK_WECHAT_WEB_SCOPE,
                stateIssueResult.state(),
                stateIssueResult.expiresInSeconds()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public AppWechatLoginResponse loginByWechat(AppWechatLoginRequest request, HttpServletRequest httpServletRequest) {
        WechatMiniappClient.WechatSession session = wechatMiniappClient.codeToSession(request.code());
        WechatBindingContext binding = new WechatBindingContext(
                UserAuthProvider.WECHAT_MINIAPP,
                session.openId(),
                session.unionId(),
                request.nickname(),
                request.avatarUrl(),
                WechatBindingSource.MINIAPP
        );
        Optional<AppUser> existingUser = findUserByWechatIdentity(binding.authProvider(), binding.openId(), binding.unionId());
        if (existingUser.isPresent()) {
            AppUser appUser = applyWechatBindingIfAbsent(existingUser.get(), binding);
            AppLoginResponse loginResponse = issueLoginResponse(appUser, httpServletRequest);
            return toWechatLoginResponse(loginResponse);
        }
        WechatBindTokenService.TokenIssueResult bindTokenResult = wechatBindTokenService.issue(
                new WechatBindTokenService.PendingWechatBinding(
                        binding.openId(),
                        binding.unionId(),
                        binding.nickname(),
                        binding.avatarUrl()
                )
        );
        return buildBindRequiredResponse(bindTokenResult.token(), bindTokenResult.expiresInSeconds());
    }

    @Transactional(rollbackFor = Exception.class)
    public AppWechatLoginResponse loginByWechatWeb(AppWechatWebLoginRequest request, HttpServletRequest httpServletRequest) {
        if (!wechatWebStateService.consume(request.state())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "WeChat login state is invalid");
        }
        WechatWebClient.WechatSession session = wechatWebClient.codeToSession(request.code());
        WechatBindingContext binding = new WechatBindingContext(
                UserAuthProvider.WECHAT_WEB,
                session.openId(),
                session.unionId(),
                null,
                null,
                WechatBindingSource.WEB
        );
        Optional<AppUser> existingUser = findUserByWechatIdentity(binding.authProvider(), binding.openId(), binding.unionId());
        if (existingUser.isPresent()) {
            AppUser appUser = applyWechatBindingIfAbsent(existingUser.get(), binding);
            AppLoginResponse loginResponse = issueLoginResponse(appUser, httpServletRequest);
            return toWechatLoginResponse(loginResponse);
        }
        WechatWebBindTokenService.TokenIssueResult bindTokenResult = wechatWebBindTokenService.issue(
                new WechatWebBindTokenService.PendingWechatWebBinding(binding.openId(), binding.unionId())
        );
        return buildBindRequiredResponse(bindTokenResult.token(), bindTokenResult.expiresInSeconds());
    }

    @Transactional(rollbackFor = Exception.class)
    public AppLoginResponse bindWechatMobile(AppWechatBindMobileRequest request, HttpServletRequest httpServletRequest) {
        validateSmsCode(request.mobile(), request.code());
        WechatBindingContext pendingBinding = resolvePendingWechatBinding(request.bindToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "WeChat binding token is invalid"));
        if (findUserByWechatIdentity(pendingBinding.authProvider(), pendingBinding.openId(), pendingBinding.unionId()).isPresent()) {
            throw new BusinessException(ErrorCode.CONFLICT, "WeChat account is already linked");
        }
        AppUser appUser = findUserByMobile(request.mobile())
                .map(existingUser -> bindWechatToExistingUser(existingUser, pendingBinding))
                .orElseGet(() -> createWechatUserWithMobile(pendingBinding, request.mobile()));
        deletePendingWechatBinding(request.bindToken(), pendingBinding.source());
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
        List<String> identities = appUserIdentityMapper.selectList(new LambdaQueryWrapper<AppUserIdentity>()
                        .eq(AppUserIdentity::getUserId, appUser.getId())
                        .eq(AppUserIdentity::getIdentityStatus, AppUserIdentityStatus.ACTIVE)
                        .eq(AppUserIdentity::getDeleted, 0)
                        .orderByDesc(AppUserIdentity::getIsPrimary)
                        .orderByAsc(AppUserIdentity::getCreatedAt))
                .stream()
                .map(identity -> identity.getIdentityType().getValue())
                .distinct()
                .toList();
        AppCurrentExpertResponse expertMode = currentAppExpertResolver.currentExpertSession()
                .map(this::toCurrentExpertResponse)
                .orElse(new AppCurrentExpertResponse(false, null, null, false));
        return new CurrentAppUserResponse(
                appUser.getId(),
                appUser.getUsername(),
                appUser.getNickname(),
                appUserAvatarUrlResolver.resolve(appUser.getId(), appUser.getAvatarUrl()),
                appUser.getMobile(),
                appUser.getEmail(),
                appUser.getProfileCompleted(),
                student == null ? null : student.getId(),
                student == null ? null : student.getCertificationStatus(),
                identities,
                expertMode
        );
    }

    private AppCurrentExpertResponse toCurrentExpertResponse(AppExpertSession expertSession) {
        return new AppCurrentExpertResponse(true, expertSession.expertId(), expertSession.realName(), true);
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
                        appUserAvatarUrlResolver.resolve(refreshed.getId(), refreshed.getAvatarUrl()),
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

    private Optional<AppUser> findUserByWechatWebOpenId(String openId) {
        return Optional.ofNullable(appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getWechatWebOpenId, openId)
                .eq(AppUser::getDeleted, 0)
                .last("LIMIT 1")));
    }

    private Optional<AppUser> findUserByWechatUnionId(String unionId) {
        return Optional.ofNullable(appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getWechatUnionId, unionId)
                .eq(AppUser::getDeleted, 0)
                .last("LIMIT 1")));
    }

    private Optional<AppUser> findUserByWechatIdentity(UserAuthProvider authProvider, String openId, String unionId) {
        Optional<AppUser> userByUnionId = hasText(unionId) ? findUserByWechatUnionId(unionId) : Optional.empty();
        Optional<AppUser> userByChannelOpenId = switch (authProvider) {
            case WECHAT_MINIAPP -> findUserByWechatOpenId(openId);
            case WECHAT_WEB -> findUserByWechatWebOpenId(openId);
            default -> Optional.empty();
        };
        if (userByUnionId.isPresent() && userByChannelOpenId.isPresent()
                && !userByUnionId.get().getId().equals(userByChannelOpenId.get().getId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "WeChat account is already linked to another user");
        }
        return userByUnionId.isPresent() ? userByUnionId : userByChannelOpenId;
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

    private AppUser createWechatUserWithMobile(WechatBindingContext binding, String mobile) {
        LocalDateTime now = LocalDateTime.now();
        AppUser appUser = new AppUser();
        appUser.setUsername("u" + mobile);
        appUser.setMobile(mobile);
        appUser.setNickname(resolveWechatNickname(binding));
        appUser.setAvatarUrl(binding.avatarUrl());
        appUser.setAuthProvider(binding.authProvider());
        assignWechatBinding(appUser, binding);
        appUser.setGender(Gender.UNKNOWN);
        appUser.setStatus(EnabledStatus.ENABLED);
        appUser.setRegisteredAt(now);
        appUser.setProfileCompleted(hasText(appUser.getNickname()) && hasText(appUser.getAvatarUrl()));
        appUser.setDeleted(0);
        appUserMapper.insert(appUser);
        return appUser;
    }

    private AppUser bindWechatToExistingUser(AppUser appUser, WechatBindingContext binding) {
        if (appUser.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "App user account is disabled");
        }
        assertWechatBindingCompatible(appUser, binding);
        boolean shouldUpdate = assignWechatBinding(appUser, binding);
        if (appUser.getAuthProvider() == null) {
            appUser.setAuthProvider(binding.authProvider());
            shouldUpdate = true;
        }
        if (Boolean.FALSE.equals(appUser.getProfileCompleted())) {
            boolean profileCompleted = hasText(appUser.getNickname()) && hasText(appUser.getAvatarUrl());
            if (profileCompleted) {
                appUser.setProfileCompleted(true);
                shouldUpdate = true;
            }
        }
        if (shouldUpdate) {
            appUserMapper.updateById(appUser);
        }
        return appUser;
    }

    private AppUser applyWechatBindingIfAbsent(AppUser appUser, WechatBindingContext binding) {
        assertWechatBindingCompatible(appUser, binding);
        boolean shouldUpdate = assignWechatBinding(appUser, binding);
        if (shouldUpdate) {
            if (Boolean.FALSE.equals(appUser.getProfileCompleted())) {
                appUser.setProfileCompleted(hasText(appUser.getNickname()) && hasText(appUser.getAvatarUrl()));
            }
            appUserMapper.updateById(appUser);
        }
        return appUser;
    }

    private void assertWechatBindingCompatible(AppUser appUser, WechatBindingContext binding) {
        switch (binding.authProvider()) {
            case WECHAT_MINIAPP -> {
                if (hasText(appUser.getWechatOpenId()) && !appUser.getWechatOpenId().equals(binding.openId())) {
                    throw new BusinessException(ErrorCode.CONFLICT, "Mobile number is already linked to another WeChat account");
                }
            }
            case WECHAT_WEB -> {
                if (hasText(appUser.getWechatWebOpenId()) && !appUser.getWechatWebOpenId().equals(binding.openId())) {
                    throw new BusinessException(ErrorCode.CONFLICT, "Mobile number is already linked to another WeChat account");
                }
            }
            default -> {
            }
        }
        if (hasText(appUser.getWechatUnionId()) && hasText(binding.unionId())
                && !appUser.getWechatUnionId().equals(binding.unionId())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Mobile number is already linked to another WeChat account");
        }
    }

    private boolean assignWechatBinding(AppUser appUser, WechatBindingContext binding) {
        boolean shouldUpdate = false;
        switch (binding.authProvider()) {
            case WECHAT_MINIAPP -> {
                if (!hasText(appUser.getWechatOpenId())) {
                    appUser.setWechatOpenId(binding.openId());
                    shouldUpdate = true;
                }
            }
            case WECHAT_WEB -> {
                if (!hasText(appUser.getWechatWebOpenId())) {
                    appUser.setWechatWebOpenId(binding.openId());
                    shouldUpdate = true;
                }
            }
            default -> {
            }
        }
        if (hasText(binding.unionId()) && !hasText(appUser.getWechatUnionId())) {
            appUser.setWechatUnionId(binding.unionId());
            shouldUpdate = true;
        }
        if (hasText(binding.nickname()) && !hasText(appUser.getNickname())) {
            appUser.setNickname(binding.nickname());
            shouldUpdate = true;
        }
        if (hasText(binding.avatarUrl()) && !hasText(appUser.getAvatarUrl())) {
            appUser.setAvatarUrl(binding.avatarUrl());
            shouldUpdate = true;
        }
        return shouldUpdate;
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

    private void validateSmsCode(String mobile, String code) {
        Object cachedCode = redisTemplate.opsForValue().get(buildSmsCodeKey(mobile));
        if (!(cachedCode instanceof String actualCode) || !actualCode.equals(code)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Sms verification code is invalid");
        }
        redisTemplate.delete(buildSmsCodeKey(mobile));
    }

    private AppWechatLoginResponse toWechatLoginResponse(AppLoginResponse loginResponse) {
        return new AppWechatLoginResponse(
                true,
                false,
                null,
                loginResponse.tokenType(),
                loginResponse.accessToken(),
                loginResponse.expiresIn(),
                loginResponse.user()
        );
    }

    private AppWechatLoginResponse buildBindRequiredResponse(String bindToken, long expiresInSeconds) {
        return new AppWechatLoginResponse(
                false,
                true,
                bindToken,
                null,
                null,
                expiresInSeconds,
                null
        );
    }

    private Optional<WechatBindingContext> resolvePendingWechatBinding(String bindToken) {
        Optional<WechatBindTokenService.PendingWechatBinding> miniappBinding = wechatBindTokenService.get(bindToken);
        if (miniappBinding.isPresent()) {
            WechatBindTokenService.PendingWechatBinding binding = miniappBinding.get();
            return Optional.of(new WechatBindingContext(
                    UserAuthProvider.WECHAT_MINIAPP,
                    binding.openId(),
                    binding.unionId(),
                    binding.nickname(),
                    binding.avatarUrl(),
                    WechatBindingSource.MINIAPP
            ));
        }
        Optional<WechatWebBindTokenService.PendingWechatWebBinding> webBinding = wechatWebBindTokenService.get(bindToken);
        if (webBinding.isPresent()) {
            WechatWebBindTokenService.PendingWechatWebBinding binding = webBinding.get();
            return Optional.of(new WechatBindingContext(
                    UserAuthProvider.WECHAT_WEB,
                    binding.openId(),
                    binding.unionId(),
                    null,
                    null,
                    WechatBindingSource.WEB
            ));
        }
        return Optional.empty();
    }

    private void deletePendingWechatBinding(String bindToken, WechatBindingSource source) {
        if (source == WechatBindingSource.MINIAPP) {
            wechatBindTokenService.delete(bindToken);
            return;
        }
        wechatWebBindTokenService.delete(bindToken);
    }

    private String resolveWechatNickname(WechatBindingContext binding) {
        if (hasText(binding.nickname())) {
            return binding.nickname();
        }
        String prefix = binding.authProvider() == UserAuthProvider.WECHAT_WEB ? "wxweb_" : "wx_";
        return prefix + binding.openId().substring(Math.max(0, binding.openId().length() - 8));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record WechatBindingContext(
            UserAuthProvider authProvider,
            String openId,
            String unionId,
            String nickname,
            String avatarUrl,
            WechatBindingSource source
    ) {
    }

    private enum WechatBindingSource {
        MINIAPP,
        WEB
    }
}
