package com.gostavdev.commercify.userservice.dto;

import java.util.Date;

public record UserDTO(
        Long userId,
        String email,
        String firstName,
        String lastName,
        Date createdAt) {
}
