package com.gugugaga.jsmedicine.module.auth.service;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminAuthorizationService adminAuthorizationService;

    public AdminUserDetailsService(AdminAuthorizationService adminAuthorizationService) {
        this.adminAuthorizationService = adminAuthorizationService;
    }

    @Override
    public AdminSecurityPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminAuthorizationInfo authorizationInfo = adminAuthorizationService.loadByUsername(username);
        if (authorizationInfo == null) {
            throw new UsernameNotFoundException(username);
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorizationInfo.roleCodes().forEach(roleCode -> authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode)));
        authorizationInfo.permissionCodes().forEach(permissionCode -> authorities.add(new SimpleGrantedAuthority(permissionCode)));
        return new AdminSecurityPrincipal(
                authorizationInfo.admin().getId(),
                authorizationInfo.admin().getUsername(),
                authorizationInfo.admin().getPasswordHash(),
                authorizationInfo.admin().getStatus() == EnabledStatus.ENABLED,
                authorities
        );
    }
}
