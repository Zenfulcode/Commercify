package com.zenfulcode.commercify.commercify.api.requests.addresses;

public record AddressRequest(
        String street,
        String city,
        String state,
        String zipCode,
        String country,
        Boolean isBilling,
        Boolean isShipping
) {
}
