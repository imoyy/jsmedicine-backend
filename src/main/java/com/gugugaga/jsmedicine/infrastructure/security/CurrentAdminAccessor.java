package com.gugugaga.jsmedicine.infrastructure.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.gugugaga.jsmedicine.module.auth.service.AdminSecurityPrincipal;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

@Component
public class CurrentAdminAccessor {

    public Optional<Long> getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof AdminSecurityPrincipal principal) {
            return Optional.of(principal.getId());
        }
        Long principalId = extractId(authentication.getPrincipal());
        if (principalId != null) {
            return Optional.of(principalId);
        }
        Long nameId = parseLong(authentication.getName());
        return Optional.ofNullable(nameId);
    }

    public Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUsername());
        }
        return Optional.ofNullable(authentication.getName());
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private Long extractId(Object principal) {
        if (principal instanceof Number number) {
            return number.longValue();
        }
        if (principal instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id != null) {
                return parseLong(String.valueOf(id));
            }
        }
        if (principal != null) {
            try {
                Method method = principal.getClass().getMethod("getId");
                Object id = method.invoke(principal);
                if (id != null) {
                    return parseLong(String.valueOf(id));
                }
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
        return null;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
