package com.zenfulcode.commercify.api.order.dto.request;

public record CustomerDetailsRequest(
        String firstName,
        String lastName,
        String email,
        String phone
) {
}
