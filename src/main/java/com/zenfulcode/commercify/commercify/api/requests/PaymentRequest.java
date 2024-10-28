package com.zenfulcode.commercify.commercify.api.requests;

public record PaymentRequest(Long orderId, String currency) {
    public String successUrl() {
        return "http://localhost:3000/checkout/success";
    }

    public String cancelUrl() {
        return "http://localhost:3000/checkout/cancel";
    }
}