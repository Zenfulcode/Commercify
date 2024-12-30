package com.zenfulcode.commercify.service.core;


import com.zenfulcode.commercify.web.dto.common.AddressDTO;
import com.zenfulcode.commercify.web.dto.common.UserDTO;
import com.zenfulcode.commercify.web.dto.mapper.AddressMapper;
import com.zenfulcode.commercify.web.dto.mapper.UserMapper;
import com.zenfulcode.commercify.domain.model.Address;
import com.zenfulcode.commercify.domain.model.User;
import com.zenfulcode.commercify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserRepository userRepository;
    private final UserMapper mapper;
    private final AddressMapper addressMapper;

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapper.apply(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(mapper);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());

        User updatedUser = userRepository.save(user);
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
    public AddressDTO setDefaultAddress(Long userId, AddressDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = Address.builder()
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDefaultAddress(null);
        return mapper.apply(userRepository.save(user));
    }

    @Transactional
    public UserDTO addRoleToUser(Long userId, String role) {
        User user = userRepository.findById(userId)
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getRoles().remove(role.toUpperCase());
        User updatedUser = userRepository.save(user);

        return mapper.apply(updatedUser);
    }
}
