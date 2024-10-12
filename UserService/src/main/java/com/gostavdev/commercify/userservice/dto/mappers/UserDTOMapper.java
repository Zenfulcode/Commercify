package com.gostavdev.commercify.userservice.dto.mappers;

import com.gostavdev.commercify.userservice.dto.UserDTO;
import com.gostavdev.commercify.userservice.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserDTOMapper implements Function<UserEntity, UserDTO> {
    @Override
    public UserDTO apply(UserEntity user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
