package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDTOMapper implements Function<UserEntity, UserDTO> {
    private final AddressDTOMapper addressDTOMapper;

    @Override
    public UserDTO apply(UserEntity user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .createdAt(user.getCreatedAt())
                .addresses(user.getAddresses().stream().map(addressDTOMapper).collect(Collectors.toList()))
                .build();
    }
}
