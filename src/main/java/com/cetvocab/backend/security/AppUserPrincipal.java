package com.cetvocab.backend.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AppUserPrincipal implements UserDetails {

    private final Long userId;
    private final String username;
    private final String passwordHash;
    private final String displayName;
    private final boolean guest;

    public AppUserPrincipal(Long userId, String username, String passwordHash,
                            String displayName, boolean guest) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.guest = guest;
    }

    public Long getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
    public boolean isGuest() { return guest; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}