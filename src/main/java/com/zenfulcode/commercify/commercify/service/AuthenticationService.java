package com.zenfulcode.commercify.commercify.service;


import com.zenfulcode.commercify.commercify.api.requests.LoginUserRequest;
import com.zenfulcode.commercify.commercify.api.requests.RegisterUserRequest;
import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.UserMapper;
import com.zenfulcode.commercify.commercify.entity.AddressEntity;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import com.zenfulcode.commercify.commercify.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserMapper mapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO registerUser(RegisterUserRequest registerRequest) {
        Optional<UserEntity> existing = userRepository.findByEmail(registerRequest.email());

        if (existing.isPresent()) {
            throw new RuntimeException("User with email " + registerRequest.email() + " already exists");
        }

        AddressEntity shippingAddress = null;

        if (registerRequest.defaultAddress() != null) {
            shippingAddress = AddressEntity.builder()
                    .street(registerRequest.defaultAddress().getStreet())
                    .city(registerRequest.defaultAddress().getCity())
                    .state(registerRequest.defaultAddress().getState())
                    .zipCode(registerRequest.defaultAddress().getZipCode())
                    .country(registerRequest.defaultAddress().getCountry())
                    .build();
        }

        UserEntity user = UserEntity.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(List.of("USER"))
                .defaultAddress(shippingAddress)
                .emailConfirmed(false)
                .build();

        UserEntity savedUser = userRepository.save(user);

        // TODO: Send user confirmation email

        return mapper.apply(savedUser);
    }

    public UserDTO registerGuest() {
        String firstName = "Guest";
        String lastName = String.valueOf(new Date().toInstant().toEpochMilli());
        String email = firstName + lastName + "@commercify.app";
        String password = UUID.randomUUID().toString();

        UserEntity user = UserEntity.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(List.of("GUEST"))
                .emailConfirmed(true)
                .build();
        UserEntity savedUser = userRepository.save(user);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        password
                )
        );

        return mapper.apply(savedUser);
    }

    public UserDTO authenticate(LoginUserRequest login) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login.email(),
                        login.password()
                )
        );

        UserEntity user = userRepository.findByEmail(login.email()).orElseThrow();

        if (!passwordEncoder.matches(login.password(), user.getPassword()))
            return null;

        return mapper.apply(user);
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
