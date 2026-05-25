package com.gugugaga.jsmedicine.module.auth.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private static final String APP_USER_ROLE = "ROLE_APP_USER";

    private final AppUserMapper appUserMapper;

    public AppUserDetailsService(AppUserMapper appUserMapper) {
        this.appUserMapper = appUserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, username)
                .eq(AppUser::getDeleted, 0)
                .last("LIMIT 1"));
        if (appUser == null || appUser.getPasswordHash() == null || appUser.getPasswordHash().isBlank()) {
            throw new UsernameNotFoundException("App user account does not exist");
        }
        if (appUser.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "App user account is disabled");
        }
        return new AppUserPrincipal(
                appUser.getId(),
                appUser.getUsername(),
                appUser.getPasswordHash(),
                true,
                List.of(new SimpleGrantedAuthority(APP_USER_ROLE))
        );
    }
}
