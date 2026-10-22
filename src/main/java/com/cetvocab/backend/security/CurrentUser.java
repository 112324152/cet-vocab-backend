package com.cetvocab.backend.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public final class CurrentUser {

    private CurrentUser() {}

    public static Long idOrNull() {
        AppUserPrincipal p = principalOrNull();
        return p == null ? null : p.getUserId();
    }

    public static Long id() {
        Long id = idOrNull();
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        return id;
    }

    public static AppUserPrincipal principalOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object p = auth.getPrincipal();
        return (p instanceof AppUserPrincipal ap) ? ap : null;
    }
}