package com.gostavdev.commercify.userservice.api.responses;

import com.gostavdev.commercify.userservice.dto.UserDTO;

public record AuthResponse(UserDTO user,  String token, long expiresIn) {
}
