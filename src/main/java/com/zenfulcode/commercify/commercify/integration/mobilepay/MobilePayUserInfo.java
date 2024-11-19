package com.zenfulcode.commercify.commercify.integration.mobilepay;

public record MobilePayUserInfo(
        String email,
        String firstName,
        String lastName,
        String phoneNumber
) {
}