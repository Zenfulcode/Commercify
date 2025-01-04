package com.zenfulcode.commercify.api.order.dto.response;

public record AddressResponse(
        String street,
        String city,
        String state,
        String zipCode,
        String country
) {
}