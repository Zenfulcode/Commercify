package com.gostavdev.commercify.userservice.service;

import com.gostavdev.commercify.userservice.UserRepository;
import com.gostavdev.commercify.userservice.api.requests.LoginUserRequest;
import com.gostavdev.commercify.userservice.api.requests.RegisterUserRequest;
import com.gostavdev.commercify.userservice.dto.UserDTO;
import com.gostavdev.commercify.userservice.dto.mappers.UserDTOMapper;
import com.gostavdev.commercify.userservice.entities.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDTOMapper mapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO registerUser(RegisterUserRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.email()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        UserEntity user = UserEntity.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(List.of("USER"))  // Default role
                .build();

        UserEntity savedUser = userRepository.save(user);
        return mapper.apply(savedUser);
    }

    public UserDTO authenticate(LoginUserRequest login) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login.email(),
                        login.password()
                )
        );

        return userRepository.findByEmail(login.email())
                .map(mapper)
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public UserDTO getAuthenticatedUser(String authHeader) {
        if (!authHeader.startsWith("Bearer ")) return null;

        final String jwt = authHeader.substring(7);
        final String email = jwtService.extractUsername(jwt);

        return userRepository.findByEmail(email)
                .map(mapper)
                .orElseThrow();
    }
}
