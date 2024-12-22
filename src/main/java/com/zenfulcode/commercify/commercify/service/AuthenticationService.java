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

import java.util.List;
import java.util.Optional;

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
        AddressEntity billingAddress;

        if (registerRequest.shippingAddress() != null) {
            shippingAddress = AddressEntity.builder()
                    .street(registerRequest.shippingAddress().getStreet())
                    .city(registerRequest.shippingAddress().getCity())
                    .state(registerRequest.shippingAddress().getState())
                    .zipCode(registerRequest.shippingAddress().getZipCode())
                    .country(registerRequest.shippingAddress().getCountry())
                    .build();
        }

        if (registerRequest.billingAddress() != null) {
            billingAddress = AddressEntity.builder()
                    .street(registerRequest.billingAddress().getStreet())
                    .city(registerRequest.billingAddress().getCity())
                    .state(registerRequest.billingAddress().getState())
                    .zipCode(registerRequest.billingAddress().getZipCode())
                    .country(registerRequest.billingAddress().getCountry())
                    .build();
        } else {
            billingAddress = shippingAddress;
        }

        UserEntity user = UserEntity.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(List.of("USER"))
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .emailConfirmed(false)
                .build();

        UserEntity savedUser = userRepository.save(user);

        // TODO: Send user confirmation email

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
