package com.zenfulcode.commercify.commercify.service;


import com.zenfulcode.commercify.commercify.api.requests.LoginUserRequest;
import com.zenfulcode.commercify.commercify.api.requests.RegisterUserRequest;
import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.UserDTOMapper;
import com.zenfulcode.commercify.commercify.entity.AddressEntity;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import com.zenfulcode.commercify.commercify.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        userRepository.findByEmail(registerRequest.email())
                .orElseThrow(() -> new RuntimeException("User with this email already exists"));

        List<AddressEntity> addresses = registerRequest.addresses().stream()
                .map(addressDTO -> AddressEntity.builder()
                        .street(addressDTO.getStreet())
                        .city(addressDTO.getCity())
                        .state(addressDTO.getState())
                        .postalCode(addressDTO.getPostalCode())
                        .country(addressDTO.getCountry())
                        .isBillingAddress(addressDTO.isBillingAddress())
                        .isShippingAddress(addressDTO.isShippingAddress())
                        .build())
                .collect(Collectors.toList());

        UserEntity user = UserEntity.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(List.of("USER"))
                .addresses(addresses)
                .build();

        addresses.forEach(address -> address.setUser(user));

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