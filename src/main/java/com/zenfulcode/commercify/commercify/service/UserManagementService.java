package com.zenfulcode.commercify.commercify.service;


import com.zenfulcode.commercify.commercify.api.requests.addresses.AddressRequest;
import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.UserMapper;
import com.zenfulcode.commercify.commercify.entity.AddressEntity;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import com.zenfulcode.commercify.commercify.repository.AddressRepository;
import com.zenfulcode.commercify.commercify.repository.UserRepository;
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
    private final AddressRepository addressRepository;

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
    public UserDTO addAddress(Long userId, AddressRequest addressDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AddressEntity address = AddressEntity.builder()
                .street(addressDTO.street())
                .city(addressDTO.city())
                .state(addressDTO.state())
                .zipCode(addressDTO.zipCode())
                .country(addressDTO.country())
                .isBillingAddress(addressDTO.isBilling())
                .isShippingAddress(addressDTO.isShipping())
                .user(user)
                .build();

        addressRepository.save(address);

        user.getAddresses().add(address);
        UserEntity updatedUser = userRepository.save(user);

        return mapper.apply(updatedUser);
    }

    @Transactional
    public UserDTO updateAddress(Long userId, Long addressId, AddressRequest addressDTO) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AddressEntity address = user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.setStreet(addressDTO.street());
        address.setCity(addressDTO.city());
        address.setState(addressDTO.state());
        address.setZipCode(addressDTO.zipCode());
        address.setCountry(addressDTO.country());
        address.setIsBillingAddress(addressDTO.isBilling());
        address.setIsShippingAddress(addressDTO.isShipping());

        UserEntity updatedUser = userRepository.save(user);
        addressRepository.save(address);

        return mapper.apply(updatedUser);
    }

    @Transactional
    public UserDTO removeAddress(Long userId, Long addressId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getAddresses().removeIf(address -> address.getId().equals(addressId));
        UserEntity updatedUser = userRepository.save(user);

        addressRepository.deleteById(addressId);

        return mapper.apply(updatedUser);
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
