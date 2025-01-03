package com.zenfulcode.commercify.api.order.dto.request;

public record AddressRequest(
        String street,
        String city,
        String state,
        String zipCode,
        String country
) {
}
