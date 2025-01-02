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
            return ResponseEntity.ok(AuthResponse.UserAuthenticated(user, "", 0));
        } catch (RuntimeException e) {
            log.error("Error registering user: {}", e.getMessage());
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

    @PutMapping("/{id}/register")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<AuthResponse> registerGuest(@PathVariable Long id, @RequestBody RegisterUserRequest request) {
        try {
            authenticationService.convertGuestToUser(id, request);

            UserDTO authenticatedUser = authenticationService.authenticate(new LoginUserRequest(request.email(), request.password()));
            String jwtToken = jwtService.generateToken(authenticatedUser);
            return ResponseEntity.ok(AuthResponse.UserAuthenticated(authenticatedUser, jwtToken, jwtService.getExpirationTime()));
        } catch (Exception e) {
            log.error("Error converting guest to a user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(AuthResponse.AuthenticationFailed(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getAuthenticatedUser(@RequestHeader("Authorization") String authHeader) {
        try {
            return ResponseEntity.ok(authenticationService.getAuthenticatedUser(authHeader));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<AuthResponse> registerGuest() {
        try {
            UserDTO user = authenticationService.registerGuest();
            String jwt = jwtService.generateToken(user);
            return ResponseEntity.ok(AuthResponse.UserAuthenticated(user, jwt, jwtService.getExpirationTime()));
        } catch (RuntimeException e) {
            log.error("Error registering guest: {}", e.getMessage());
            return ResponseEntity.badRequest().body(AuthResponse.AuthenticationFailed(e.getMessage()));
        }
    }
}