package com.gostavdev.commercify.userservice.controllers;

import com.gostavdev.commercify.userservice.api.requests.LoginUserRequest;
import com.gostavdev.commercify.userservice.api.requests.RegisterUserRequest;
import com.gostavdev.commercify.userservice.dto.UserDTO;
import com.gostavdev.commercify.userservice.api.responses.AuthResponse;
import com.gostavdev.commercify.userservice.service.AuthenticationService;
import com.gostavdev.commercify.userservice.service.JwtService;
import com.gostavdev.commercify.userservice.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterUserRequest registerRequest) {
        UserDTO user = authenticationService.registerUser(registerRequest);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginUserRequest loginRequest) {
        UserDTO authenticatedUser = authenticationService.authenticate(loginRequest);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        AuthResponse authResponse = new AuthResponse(jwtToken, jwtService.getExpirationTime());

        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDTO> getAuthenticatedUser(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(authenticationService.getAuthenticatedUser(authHeader));
    }
}
