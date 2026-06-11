package com.gugugaga.jsmedicine.module.expert.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityStatus;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityType;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.auth.app.service.CurrentAppUserResolver;
import com.gugugaga.jsmedicine.module.expert.app.entity.AppExpertSession;
import com.gugugaga.jsmedicine.module.expert.entity.Expert;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertMapper;
import com.gugugaga.jsmedicine.module.user.entity.AppUserIdentity;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserIdentityMapper;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class CurrentAppExpertResolver {

    private final CurrentAppUserResolver currentAppUserResolver;
    private final AppUserIdentityMapper appUserIdentityMapper;
    private final ExpertMapper expertMapper;

    public CurrentAppExpertResolver(
            CurrentAppUserResolver currentAppUserResolver,
            AppUserIdentityMapper appUserIdentityMapper,
            ExpertMapper expertMapper
    ) {
        this.currentAppUserResolver = currentAppUserResolver;
        this.appUserIdentityMapper = appUserIdentityMapper;
        this.expertMapper = expertMapper;
    }

    public Optional<AppExpertSession> currentExpertSession() {
        Optional<AppUserSession> sessionOptional = currentAppUserResolver.currentSession();
        if (sessionOptional.isEmpty()) {
            return Optional.empty();
        }
        Long userId = sessionOptional.get().userId();
        if (!hasActiveExpertIdentity(userId)) {
            return Optional.empty();
        }
        Expert expert = expertMapper.selectOne(new LambdaQueryWrapper<Expert>()
                .eq(Expert::getUserId, userId)
                .eq(Expert::getDeleted, 0)
                .last("LIMIT 1"));
        if (expert == null
                || expert.getStatus() != EnabledStatus.ENABLED
                || expert.getConsultEnabled() != EnabledStatus.ENABLED) {
            return Optional.empty();
        }
        return Optional.of(new AppExpertSession(userId, expert.getId(), expert.getRealName()));
    }

    public AppExpertSession requireCurrentExpert() {
        return currentExpertSession()
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "Current app user is not an active expert"));
    }

    private boolean hasActiveExpertIdentity(Long userId) {
        AppUserIdentity identity = appUserIdentityMapper.selectOne(new LambdaQueryWrapper<AppUserIdentity>()
                .eq(AppUserIdentity::getUserId, userId)
                .eq(AppUserIdentity::getIdentityType, AppUserIdentityType.EXPERT)
                .eq(AppUserIdentity::getDeleted, 0)
                .last("LIMIT 1"));
        return identity != null && Objects.equals(identity.getIdentityStatus(), AppUserIdentityStatus.ACTIVE);
    }
}
