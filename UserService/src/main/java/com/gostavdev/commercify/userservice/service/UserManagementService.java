package com.gostavdev.commercify.userservice.service;

import com.gostavdev.commercify.userservice.UserRepository;
import com.gostavdev.commercify.userservice.api.requests.RegisterUserRequest;
import com.gostavdev.commercify.userservice.dto.UserDTO;
import com.gostavdev.commercify.userservice.dto.mappers.UserDTOMapper;
import com.gostavdev.commercify.userservice.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserRepository userRepository;
    private final UserDTOMapper mapper;

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapper.apply(user);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());

        UserEntity updatedUser = userRepository.save(user);
        return mapper.apply(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
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

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .map(mapper)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
