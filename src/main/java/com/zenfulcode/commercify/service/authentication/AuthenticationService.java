package com.zenfulcode.commercify.service.authentication;


import com.zenfulcode.commercify.web.dto.request.auth.LoginUserRequest;
import com.zenfulcode.commercify.web.dto.request.auth.RegisterUserRequest;
import com.zenfulcode.commercify.web.dto.common.UserDTO;
import com.zenfulcode.commercify.web.dto.mapper.UserMapper;
import com.zenfulcode.commercify.domain.model.Address;
import com.zenfulcode.commercify.domain.model.User;
import com.zenfulcode.commercify.repository.UserRepository;
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
        Optional<User> existing = userRepository.findByEmail(registerRequest.email());

        if (existing.isPresent()) {
            throw new RuntimeException("User with email " + registerRequest.email() + " already exists");
        }

        Address shippingAddress = null;

        if (registerRequest.defaultAddress() != null) {
            shippingAddress = Address.builder()
                    .street(registerRequest.defaultAddress().getStreet())
                    .city(registerRequest.defaultAddress().getCity())
                    .state(registerRequest.defaultAddress().getState())
                    .zipCode(registerRequest.defaultAddress().getZipCode())
                    .country(registerRequest.defaultAddress().getCountry())
                    .build();
        }

        User user = User.builder()
                .firstName(registerRequest.firstName())
                .lastName(registerRequest.lastName())
                .email(registerRequest.email())
                .password(passwordEncoder.encode(registerRequest.password()))
                .roles(List.of("USER"))
                .defaultAddress(shippingAddress)
                .emailConfirmed(false)
                .build();

        User savedUser = userRepository.save(user);

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

        User user = userRepository.findByEmail(login.email()).orElseThrow();

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
