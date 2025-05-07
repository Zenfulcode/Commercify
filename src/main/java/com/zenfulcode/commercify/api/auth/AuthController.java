package com.zenfulcode.commercify.api.auth;

import com.zenfulcode.commercify.api.auth.dto.request.LoginRequest;
import com.zenfulcode.commercify.api.auth.dto.request.RefreshTokenRequest;
import com.zenfulcode.commercify.api.auth.dto.request.RegisterRequest;
import com.zenfulcode.commercify.api.auth.dto.response.AuthResponse;
import com.zenfulcode.commercify.api.auth.dto.response.NextAuthResponse;
import com.zenfulcode.commercify.auth.application.service.AuthenticationApplicationService;
import com.zenfulcode.commercify.auth.application.service.AuthenticationResult;
import com.zenfulcode.commercify.auth.domain.exception.InvalidAuthenticationException;
import com.zenfulcode.commercify.auth.domain.model.AuthenticatedUser;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import com.zenfulcode.commercify.user.application.dto.response.UserProfileResponse;
import com.zenfulcode.commercify.user.application.service.UserApplicationService;
import com.zenfulcode.commercify.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationApplicationService authService;
    private final UserApplicationService userService;
    private final UserApplicationService userApplicationService;

    @PostMapping("/nextauth")
    public ResponseEntity<ApiResponse<NextAuthResponse>> nextAuthSignIn(@RequestBody LoginRequest request) {
        log.info("Next auth request: {}", request);

        // Authenticate through the application service
        AuthenticationResult result = authService.authenticate(request.toCommand());

        // Create and return the NextAuth response
        return ResponseEntity.ok(ApiResponse.success(NextAuthResponse.from(result)));
    }

    @GetMapping("/session")
    public ResponseEntity<ApiResponse<NextAuthResponse>> validateSession(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract token using a domain service method
            String token = authService.extractTokenFromHeader(authHeader).orElseThrow(() -> new InvalidAuthenticationException("Invalid authorization header"));

            // Validate token through the application service
            AuthenticatedUser user = authService.validateAccessToken(token);

            // Create and return the NextAuth session response
            return ResponseEntity.ok(ApiResponse.success(NextAuthResponse.fromUser(user)));
        } catch (InvalidAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        System.out.println("Authorization Header: " + authHeader);

        // Extract token using a domain service method
        String token = authService.extractTokenFromHeader(authHeader).orElseThrow(() -> new InvalidAuthenticationException("Invalid authorization header"));

        // Validate token through the application service
        AuthenticatedUser authenticatedUser = authService.validateAccessToken(token);

        // Fetch full user entity from the database
        User user = userApplicationService.getUser(authenticatedUser.getUserId());

        // Map to response DTO
        UserProfileResponse response = UserProfileResponse.fromUser(user);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthenticationResult result = authService.authenticate(request.toCommand());
        AuthResponse response = AuthResponse.from(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {

        userService.registerUser(request.firstName(), request.lastName(), request.email(), request.password(), request.phone());

        // Authenticate the newly registered user
        AuthenticationResult result = authService.authenticate(new LoginRequest(request.email(), request.password(), false).toCommand());

        AuthResponse response = AuthResponse.from(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {

        AuthenticationResult result = authService.refreshToken(request.refreshToken());
        AuthResponse response = AuthResponse.from(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
