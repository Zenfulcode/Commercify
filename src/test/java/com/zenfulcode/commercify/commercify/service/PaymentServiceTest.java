package com.zenfulcode.commercify.commercify.service;


import com.zenfulcode.commercify.commercify.PaymentProvider;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.responses.CancelPaymentResponse;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StripeService stripeService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentEntity paymentEntity;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentEntity = PaymentEntity.builder()
                .id(1L)
                .orderId(1L)
                .status(PaymentStatus.PENDING)
                .paymentProvider(PaymentProvider.STRIPE)
                .totalAmount(100.0)
                .build();

        paymentRequest = new PaymentRequest(1L, "USD");
    }

    @Test
    @DisplayName("Should get payment status successfully")
    void getPaymentStatus_Success() {
        when(paymentRepository.findByOrderId(anyLong())).thenReturn(Optional.of(paymentEntity));

        PaymentStatus status = paymentService.getPaymentStatus(1L);

        assertEquals(PaymentStatus.PENDING, status);
        verify(paymentRepository).findByOrderId(1L);
    }

    @Test
    @DisplayName("Should return NOT_FOUND status when payment doesn't exist")
    void getPaymentStatus_NotFound() {
        when(paymentRepository.findByOrderId(anyLong())).thenReturn(Optional.empty());

        PaymentStatus status = paymentService.getPaymentStatus(1L);

        assertEquals(PaymentStatus.NOT_FOUND, status);
        verify(paymentRepository).findByOrderId(1L);
    }

    @Test
    @DisplayName("Should cancel payment successfully")
    void cancelPayment_Success() {
        paymentEntity.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByOrderId(anyLong())).thenReturn(Optional.of(paymentEntity));
        when(stripeService.cancelPayment(anyLong())).thenReturn(
                new CancelPaymentResponse(true, "Payment cancelled successfully")
        );

        CancelPaymentResponse response = paymentService.cancelPayment(1L);

        assertTrue(response.success());
        verify(paymentRepository).findByOrderId(1L);
        verify(stripeService).cancelPayment(1L);
    }

    @Test
    @DisplayName("Should handle payment not found during cancellation")
    void cancelPayment_NotFound() {
        when(paymentRepository.findByOrderId(anyLong())).thenReturn(Optional.empty());

        CancelPaymentResponse response = paymentService.cancelPayment(1L);

        assertFalse(response.success());
        assertEquals("Payment not found", response.message());
        verify(paymentRepository).findByOrderId(1L);
        verify(stripeService, never()).cancelPayment(anyLong());
    }

    @Test
    @DisplayName("Should make payment successfully with Stripe")
    void makePayment_StripeSuccess() {
        PaymentResponse expectedResponse = new PaymentResponse(1L, PaymentStatus.PAID, "https://stripe.com/checkout");
        when(stripeService.checkoutSession(any(), any())).thenReturn(expectedResponse);

        PaymentResponse response = paymentService.makePayment(PaymentProvider.STRIPE, paymentRequest);

        assertNotNull(response);
        assertEquals(PaymentStatus.PAID, response.status());
        verify(stripeService).checkoutSession(eq(paymentRequest), any());
    }

    @Test
    @DisplayName("Should handle failed payment")
    void makePayment_Failed() {
        when(stripeService.checkoutSession(any(), any())).thenReturn(PaymentResponse.FailedPayment());

        PaymentResponse response = paymentService.makePayment(PaymentProvider.STRIPE, paymentRequest);

        assertEquals(PaymentStatus.FAILED, response.status());
        assertEquals(-1L, response.paymentId());
        verify(stripeService).checkoutSession(eq(paymentRequest), any());
    }
}