package com.zenfulcode.commercify.commercify.controller;


import com.zenfulcode.commercify.commercify.api.requests.LoginUserRequest;
import com.zenfulcode.commercify.commercify.api.requests.RegisterUserRequest;
import com.zenfulcode.commercify.commercify.api.responses.AuthResponse;
import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.service.AuthenticationService;
import com.zenfulcode.commercify.commercify.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterUserRequest registerRequest) {
        try {
            UserDTO user = authenticationService.registerUser(registerRequest);

            if (registerRequest.isGuest()) {
                UserDTO authenticated = authenticationService.authenticate(new LoginUserRequest(registerRequest.email(), registerRequest.password()));
                String jwt = jwtService.generateToken(authenticated);
                return ResponseEntity.ok(AuthResponse.UserAuthenticated(authenticated, jwt, jwtService.getExpirationTime()));
            }

            return ResponseEntity.ok(AuthResponse.UserAuthenticated(user, "", 0));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(AuthResponse.AuthenticationFailed(e.getMessage()));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginUserRequest loginRequest) {
        try {
            UserDTO authenticatedUser = authenticationService.authenticate(loginRequest);
            String jwtToken = jwtService.generateToken(authenticatedUser);
            return ResponseEntity.ok(AuthResponse.UserAuthenticated(authenticatedUser, jwtToken, jwtService.getExpirationTime()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AuthResponse.AuthenticationFailed(e.getMessage()));
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDTO> getAuthenticatedUser(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authenticationService.getAuthenticatedUser(authHeader));
    }
}
