package com.zenfulcode.commercify.integration.mobilepay;

import com.zenfulcode.commercify.domain.enums.PaymentStatus;
import com.zenfulcode.commercify.domain.model.Payment;
import com.zenfulcode.commercify.exception.PaymentProcessingException;
import com.zenfulcode.commercify.repository.PaymentRepository;
import com.zenfulcode.commercify.service.core.PaymentService;
import com.zenfulcode.commercify.service.integration.mobilepay.MobilePayService;
import com.zenfulcode.commercify.service.integration.mobilepay.MobilePayTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobilePayServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MobilePayTokenService tokenService;

    @InjectMocks
    private MobilePayService mobilePayService;

    private Payment payment;
    private static final String PAYMENT_REFERENCE = "test-reference";

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .mobilePayReference(PAYMENT_REFERENCE)
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("Should handle successful payment callback")
    void handlePaymentCallback_Success() {
        when(paymentRepository.findByMobilePayReference(PAYMENT_REFERENCE))
                .thenReturn(Optional.of(payment));

        mobilePayService.handlePaymentCallback(PAYMENT_REFERENCE, "AUTHORIZED");

        verify(paymentService).handlePaymentStatusUpdate(eq(1L), eq(PaymentStatus.PAID));
    }

    @Test
    @DisplayName("Should handle payment not found in callback")
    void handlePaymentCallback_PaymentNotFound() {
        when(paymentRepository.findByMobilePayReference(PAYMENT_REFERENCE))
                .thenReturn(Optional.empty());

        assertThrows(PaymentProcessingException.class, () ->
                mobilePayService.handlePaymentCallback(PAYMENT_REFERENCE, "AUTHORIZED"));
    }

    @Test
    @DisplayName("Should handle aborted payment")
    void handlePaymentCallback_Aborted() {
        when(paymentRepository.findByMobilePayReference(PAYMENT_REFERENCE))
                .thenReturn(Optional.of(payment));

        mobilePayService.handlePaymentCallback(PAYMENT_REFERENCE, "ABORTED");

        verify(paymentService).handlePaymentStatusUpdate(eq(1L), eq(PaymentStatus.CANCELLED));
    }

    @Test
    @DisplayName("Should handle expired payment")
    void handlePaymentCallback_Expired() {
        when(paymentRepository.findByMobilePayReference(PAYMENT_REFERENCE))
                .thenReturn(Optional.of(payment));

        mobilePayService.handlePaymentCallback(PAYMENT_REFERENCE, "EXPIRED");

        verify(paymentService).handlePaymentStatusUpdate(eq(1L), eq(PaymentStatus.EXPIRED));
    }
}