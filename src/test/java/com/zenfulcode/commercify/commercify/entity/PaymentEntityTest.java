package com.zenfulcode.commercify.commercify.entity;


import com.zenfulcode.commercify.commercify.PaymentProvider;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentEntityTest {

    private PaymentEntity payment;

    @BeforeEach
    void setUp() {
        payment = PaymentEntity.builder()
                .id(1L)
                .orderId(1L)
                .stripePaymentIntent("pi_123")
                .totalAmount(199.99)
                .paymentMethod("Credit Card")
                .paymentProvider(PaymentProvider.STRIPE)
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should create payment with builder pattern")
    void testPaymentBuilder() {
        assertNotNull(payment);
        assertEquals(1L, payment.getId());
        assertEquals(1L, payment.getOrderId());
        assertEquals("pi_123", payment.getStripePaymentIntent());
        assertEquals(199.99, payment.getTotalAmount());
        assertEquals("Credit Card", payment.getPaymentMethod());
        assertEquals(PaymentProvider.STRIPE, payment.getPaymentProvider());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
    }

    @Test
    @DisplayName("Should update payment status")
    void testPaymentStatusUpdate() {
        payment.setStatus(PaymentStatus.PAID);
        assertEquals(PaymentStatus.PAID, payment.getStatus());
    }

    @Test
    @DisplayName("Should handle timestamps")
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        assertThrows(DateTimeException.class, () -> payment.setCreatedAt(Instant.from(now)));
        assertThrows(DateTimeException.class, () -> payment.setUpdatedAt(Instant.from(now)));
    }

    @Test
    @DisplayName("Should validate payment provider")
    void testPaymentProvider() {
        payment.setPaymentProvider(PaymentProvider.STRIPE);
        assertEquals(PaymentProvider.STRIPE, payment.getPaymentProvider());
    }
}