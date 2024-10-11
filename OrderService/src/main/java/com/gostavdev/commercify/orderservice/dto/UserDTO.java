package com.gostavdev.commercify.orderservice.dto;

import java.util.Date;

public record UserDTO(
        String userId,
        String email,
        String firstName,
        String lastName,
        Date createdAt) {
}
