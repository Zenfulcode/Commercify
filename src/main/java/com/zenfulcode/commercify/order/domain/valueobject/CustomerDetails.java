package com.zenfulcode.commercify.order.domain.valueobject;

public record CustomerDetails(
        String firstName,
        String lastName,
        String email,
        String phone
) {
    public CustomerDetails {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
    }
}
