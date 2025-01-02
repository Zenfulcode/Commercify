package com.zenfulcode.commercify.commercify.service;


import com.zenfulcode.commercify.commercify.api.requests.RegisterUserRequest;
import com.zenfulcode.commercify.commercify.dto.AddressDTO;
import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.AddressMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.UserMapper;
import com.zenfulcode.commercify.commercify.entity.AddressEntity;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import com.zenfulcode.commercify.commercify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {
    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final AddressMapper addressMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapper.apply(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapper);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) throws RuntimeException {  // Explicitly declare throws
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<UserEntity> existing = userRepository.findByEmail(userDTO.getEmail());

        if (existing.isPresent() && !existing.get().getId().equals(id)) {  // Add check for same user
            throw new RuntimeException("User with email " + userDTO.getEmail() + " already exists");
        }

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());

        return mapper.apply(userRepository.save(user));
    }

    @Transactional
    public UserDTO updateGuest(Long id, RegisterUserRequest request) {
        try {
            updateUser(id, request.toUserDTO());

            UserEntity user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setPassword(passwordEncoder.encode(request.password()));
            user.removeRole("GUEST");
            user.addRole("USER");

            UserEntity updatedUser = userRepository.save(user);
            return mapper.apply(updatedUser);
        } catch (RuntimeException e) {
            // Log the error
            log.error("Failed to update guest user: {}", e.getMessage(), e);
            throw e;  // Re-throw the exception instead of swallowing it
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public AddressDTO setDefaultAddress(Long userId, AddressDTO request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AddressEntity address = AddressEntity.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .build();

        user.setDefaultAddress(address);
        userRepository.save(user);

        return addressMapper.apply(address);
    }

    @Transactional
    public UserDTO removeDefaultAddress(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDefaultAddress(null);
        return mapper.apply(userRepository.save(user));
    }

    @Transactional
    public UserDTO addRoleToUser(Long userId, String role) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRoles() == null) {
            user.setRoles(new ArrayList<>());
        }

        if (!user.getRoles().contains(role.toUpperCase())) {
            user.getRoles().add(role.toUpperCase());
            userRepository.save(user);
        }

        return mapper.apply(user);
    }

    @Transactional
    public UserDTO removeRoleFromUser(Long userId, String role) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getRoles().remove(role.toUpperCase());
        UserEntity updatedUser = userRepository.save(user);

        return mapper.apply(updatedUser);
    }
}
