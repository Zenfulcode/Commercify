package com.zenfulcode.commercify.commercify.service;


import com.zenfulcode.commercify.commercify.api.requests.LoginUserRequest;
import com.zenfulcode.commercify.commercify.api.requests.RegisterUserRequest;
import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.UserMapper;
import com.zenfulcode.commercify.commercify.entity.AddressEntity;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import com.zenfulcode.commercify.commercify.repository.AddressRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AuthenticationService {
    private final AddressRepository addressRepository;
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
        } else {
            log.info("Creating user with email: {}", registerRequest.email());
        }

        Set<AddressEntity> addresses = registerRequest.addresses().stream()
                .map(addressDTO -> AddressEntity.builder()
                        .street(addressDTO.getStreet())
                        .city(addressDTO.getCity())
                        .state(addressDTO.getState())
                        .zipCode(addressDTO.getZipCode())
                        .country(addressDTO.getCountry())
                        .isBillingAddress(addressDTO.getIsBilling())
                        .isShippingAddress(addressDTO.getIsShipping())
                        .build())
                .collect(Collectors.toSet());

        UserEntity user = UserEntity.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(List.of("USER"))
                .addresses(addresses)
                .build();

        addresses.forEach(address -> address.setUser(user));

        addressRepository.saveAll(addresses);

        log.info("addresse: {}", addresses);

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
