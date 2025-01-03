package com.zenfulcode.commercify.commercify.component;

import com.zenfulcode.commercify.commercify.entity.AddressEntity;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import com.zenfulcode.commercify.commercify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AdminDataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String email;
    @Value("${admin.password}")
    private String password;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (userRepository.findByEmail(email).isEmpty()) {
                AddressEntity defaultAddress = AddressEntity.builder()
                        .street("123 Main St")
                        .city("Springfield")
                        .state("IL")
                        .zipCode("62701")
                        .country("US")
                        .build();

                UserEntity adminUser = UserEntity.builder()
                        .email(email)
                        .password(passwordEncoder.encode(password))
                        .firstName("Admin")
                        .lastName("User")
                        .roles(List.of("ADMIN", "USER"))
                        .defaultAddress(defaultAddress)
                        .emailConfirmed(true)
                        .build();

                userRepository.save(adminUser);
                System.out.println("Admin user created successfully.");
            } else {
                System.out.println("Admin user already exists. Skipping creation.");
            }
        };
    }
}