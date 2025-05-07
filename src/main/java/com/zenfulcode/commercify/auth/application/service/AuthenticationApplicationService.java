package com.zenfulcode.commercify.auth.application.service;

import com.zenfulcode.commercify.auth.application.command.LoginCommand;
import com.zenfulcode.commercify.auth.domain.event.UserAuthenticatedEvent;
import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;
import com.zenfulcode.commercify.auth.domain.service.AuthenticationDomainService;
import com.zenfulcode.commercify.auth.infrastructure.security.TokenService;
import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.user.application.command.CreateUserCommand;
import com.zenfulcode.commercify.user.application.service.UserApplicationService;
import com.zenfulcode.commercify.user.domain.exception.UserAlreadyExistsException;
import com.zenfulcode.commercify.user.domain.exception.UserNotFoundException;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserRole;
import com.zenfulcode.commercify.user.domain.repository.UserRepository;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class AuthenticationApplicationService {
    private final AuthenticationManager authenticationManager;
    private final AuthenticationDomainService authenticationDomainService;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final DomainEventPublisher eventPublisher;
    private final UserApplicationService userApplicationService;

    @Transactional
    public AuthenticationResult authenticate(LoginCommand command) {
        String email = command.email();
        String password = command.password();

        UserId userId;
        if (!command.isGuest()) {
            User user = userRepository.findByEmail(command.email()).orElseThrow(() -> new UserNotFoundException(command.email()));

            userId = user.getId();
        } else {
            Optional<User> user = userRepository.findByEmail(command.email());
            if (user.isPresent()) {
                throw new UserAlreadyExistsException("There is already a user with this email");
            }

            email = "guest-" + System.currentTimeMillis() + "@commercify.com";
            password = UUID.randomUUID().toString();

            CreateUserCommand createUserCommand = new CreateUserCommand(email, "Guest", "User", password, Set.of(UserRole.ROLE_GUEST), null);

            userId = userApplicationService.createUser(createUserCommand);
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        User user = userApplicationService.getUser(userId);

        // Generate tokens
        String accessToken = tokenService.generateAccessToken(authenticatedUser);
        String refreshToken = tokenService.generateRefreshToken(authenticatedUser);

        // Publish domain event
        eventPublisher.publish(new UserAuthenticatedEvent(this, userId, email, command.isGuest()));

        return new AuthenticationResult(accessToken, refreshToken, authenticatedUser, user);
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser validateAccessToken(String token) {
        String userId = tokenService.validateTokenAndGetUserId(token);
        log.info("Validating access token for user: '{}'", userId);

        User user = userRepository.findById(UserId.of(userId)).orElseThrow();
        return authenticationDomainService.createAuthenticatedUser(user);
    }

    @Transactional
    public AuthenticationResult refreshToken(String refreshToken) {
        String userId = tokenService.validateTokenAndGetUserId(refreshToken);
        User user = userRepository.findById(UserId.of(userId)).orElseThrow();

        AuthenticatedUser authenticatedUser = authenticationDomainService.createAuthenticatedUser(user);

        String newAccessToken = tokenService.generateAccessToken(authenticatedUser);
        String newRefreshToken = tokenService.generateRefreshToken(authenticatedUser);

        return new AuthenticationResult(newAccessToken, newRefreshToken, authenticatedUser, user);
    }

    public Optional<String> extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return Optional.of(authHeader.substring(7));
    }
}