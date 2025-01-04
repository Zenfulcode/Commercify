package com.zenfulcode.commercify.auth.application.service;

import com.zenfulcode.commercify.auth.domain.event.UserAuthenticatedEvent;
import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;
import com.zenfulcode.commercify.auth.domain.model.UserRole;
import com.zenfulcode.commercify.auth.domain.service.AuthenticationDomainService;
import com.zenfulcode.commercify.auth.infrastructure.security.TokenService;
import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.repository.UserRepository;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationApplicationService {
    private final AuthenticationManager authenticationManager;
    private final AuthenticationDomainService authenticationDomainService;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public AuthenticationResult authenticate(String email, String password) {
        // Authenticate using Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        // Generate tokens
        String accessToken = tokenService.generateAccessToken(authenticatedUser);
        String refreshToken = tokenService.generateRefreshToken(authenticatedUser);

        // Publish domain event
        User user = userRepository.findByEmail(email)
                .orElseThrow(); // User must exist at this point
        eventPublisher.publish(new UserAuthenticatedEvent(user.getId(), email));

        return new AuthenticationResult(accessToken, refreshToken, authenticatedUser);
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser validateAccessToken(String token) {
        String userId = tokenService.validateTokenAndGetUserId(token);
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow();
        return authenticationDomainService.createAuthenticatedUser(user);
    }

    @Transactional
    public AuthenticationResult refreshToken(String refreshToken) {
        String userId = tokenService.validateTokenAndGetUserId(refreshToken);
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow();

        AuthenticatedUser authenticatedUser = authenticationDomainService.createAuthenticatedUser(user);

        String newAccessToken = tokenService.generateAccessToken(authenticatedUser);
        String newRefreshToken = tokenService.generateRefreshToken(authenticatedUser);

        return new AuthenticationResult(newAccessToken, newRefreshToken, authenticatedUser);
    }

    public Set<UserRole> mapRoles(Collection<com.zenfulcode.commercify.user.domain.model.UserRole> roles) {
        return roles.stream()
                .map(role -> UserRole.valueOf("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }
}