package com.zenfulcode.commercify.auth.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthenticatedUser implements UserDetails {
    private String userId;
    private String username;
    private String email;
    private String password;
    private Set<UserRole> roles;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;

    private AuthenticatedUser(
            String userId,
            String username,
            String email,
            String password,
            Set<UserRole> roles,
            boolean enabled,
            boolean accountNonExpired,
            boolean accountNonLocked,
            boolean credentialsNonExpired
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public static AuthenticatedUser create(
            String userId,
            String username,
            String email,
            String password,
            Set<UserRole> roles,
            boolean enabled,
            boolean accountNonExpired,
            boolean accountNonLocked,
            boolean credentialsNonExpired
    ) {
        return new AuthenticatedUser(
                userId,
                username,
                email,
                password,
                roles,
                enabled,
                accountNonExpired,
                accountNonLocked,
                credentialsNonExpired
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
