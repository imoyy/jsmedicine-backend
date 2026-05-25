package com.gugugaga.jsmedicine.infrastructure.security;

import com.gugugaga.jsmedicine.module.auth.entity.AdminSession;
import com.gugugaga.jsmedicine.module.auth.service.AdminSecurityPrincipal;
import com.gugugaga.jsmedicine.module.auth.service.AuthTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthTokenService authTokenService;

    public BearerTokenAuthenticationFilter(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        resolveToken(request)
                .ifPresent(token -> authTokenService.getSession(token).ifPresent(session -> setAuthentication(session, token)));
        filterChain.doFilter(request, response);
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        return token.isBlank() ? Optional.empty() : Optional.of(token);
    }

    private void setAuthentication(AdminSession session, String token) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        session.roleCodes().forEach(roleCode -> authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode)));
        session.permissionCodes().forEach(permissionCode -> authorities.add(new SimpleGrantedAuthority(permissionCode)));
        AdminSecurityPrincipal principal = new AdminSecurityPrincipal(
                session.adminId(),
                session.username(),
                "",
                true,
                authorities
        );
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                token,
                authorities
        );
        authentication.setDetails(session);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
