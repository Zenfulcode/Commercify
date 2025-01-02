package com.zenfulcode.commercify.commercify.api.requests;

import com.zenfulcode.commercify.commercify.dto.AddressDTO;
import com.zenfulcode.commercify.commercify.dto.UserDTO;

public record RegisterUserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        AddressDTO defaultAddress) {
    public UserDTO toUserDTO() {
        return new UserDTO(null, email, firstName, lastName, null, defaultAddress, null);
    }
}