package com.zenfulcode.commercify.api.auth;

import com.zenfulcode.commercify.api.auth.dto.request.LoginRequest;
import com.zenfulcode.commercify.api.auth.dto.request.RefreshTokenRequest;
import com.zenfulcode.commercify.api.auth.dto.request.RegisterRequest;
import com.zenfulcode.commercify.api.auth.dto.response.AuthResponse;
import com.zenfulcode.commercify.auth.application.service.AuthenticationApplicationService;
import com.zenfulcode.commercify.auth.application.service.AuthenticationResult;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import com.zenfulcode.commercify.user.application.service.UserApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationApplicationService authService;
    private final UserApplicationService userService;

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request) {
        AuthenticationResult result = authService.authenticate(
                request.email(),
                request.password()
        );

        AuthResponse response = AuthResponse.from(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody RegisterRequest request) {

        userService.registerUser(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password(),
                request.phone()
        );

        // Authenticate the newly registered user
        AuthenticationResult result = authService.authenticate(
                request.email(),
                request.password()
        );

        AuthResponse response = AuthResponse.from(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        AuthenticationResult result = authService.refreshToken(request.refreshToken());
        AuthResponse response = AuthResponse.from(result);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
