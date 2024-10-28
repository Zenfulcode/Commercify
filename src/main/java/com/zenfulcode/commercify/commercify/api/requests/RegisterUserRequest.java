package com.zenfulcode.commercify.commercify.api.requests;


import com.zenfulcode.commercify.commercify.dto.AddressDTO;

import java.util.List;

public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        List<AddressDTO> addresses) {
}