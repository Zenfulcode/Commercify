package com.zenfulcode.commercify.commercify.service;


import com.zenfulcode.commercify.commercify.api.requests.LoginUserRequest;
import com.zenfulcode.commercify.commercify.api.requests.RegisterUserRequest;
import com.zenfulcode.commercify.commercify.dto.AddressDTO;
import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.UserMapper;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import com.zenfulcode.commercify.commercify.repository.AddressRepository;
import com.zenfulcode.commercify.commercify.repository.UserRepository;
import com.zenfulcode.commercify.commercify.service.email.EmailConfirmationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private EmailConfirmationService emailConfirmationService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterUserRequest registerRequest;
    private UserEntity userEntity;
    private UserDTO userDTO;
    private LoginUserRequest loginRequest;

    @BeforeEach
    void setUp() {
        AddressDTO shippingAddress = AddressDTO.builder()
                .street("123 Test St")
                .city("Test City")
                .state("Test State")
                .zipCode("12345")
                .country("Test Country")
                .build();

        registerRequest = new RegisterUserRequest(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                false,
                shippingAddress,
                null
        );

        userEntity = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_password")
                .firstName("John")
                .lastName("Doe")
                .roles(List.of("USER"))
                .emailConfirmed(true)
                .build();

        userDTO = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .roles(List.of("USER"))
                .shippingAddress(shippingAddress)
                .build();

        loginRequest = new LoginUserRequest("test@example.com", "password123");
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void registerUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.apply(any(UserEntity.class))).thenReturn(userDTO);

        UserDTO result = authenticationService.registerUser(registerRequest);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).apply(any(UserEntity.class));
    }

    @Test
    void registerUser_NoPasswordProvided_ShouldSetDefaultPassword() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest("test@example.com", "", "Test", "User",
                false,
                null,
                null);

        System.out.println(request);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(userMapper.apply(any(UserEntity.class))).thenReturn(userDTO);

        // Act
        UserDTO result = authenticationService.registerUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void registerUser_ExistingEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));

        assertThrows(RuntimeException.class, () -> authenticationService.registerUser(registerRequest));

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should successfully authenticate user")
    void authenticate_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(userMapper.apply(any(UserEntity.class))).thenReturn(userDTO);

        UserDTO result = authenticationService.authenticate(loginRequest);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("test@example.com", "password123")
        );
    }

    @Test
    @DisplayName("Should get authenticated user details")
    void getAuthenticatedUser_Success() {
        String jwt = "Bearer valid_jwt_token";
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(userMapper.apply(any(UserEntity.class))).thenReturn(userDTO);

        UserDTO result = authenticationService.getAuthenticatedUser(jwt);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(jwtService).extractUsername("valid_jwt_token");
        verify(userRepository).findByEmail("test@example.com");
    }
}