package com.zenfulcode.commercify.api.order.dto.response;

public record CustomerDetailsResponse(
        String firstName,
        String lastName,
        String email,
        String phone
) {
}