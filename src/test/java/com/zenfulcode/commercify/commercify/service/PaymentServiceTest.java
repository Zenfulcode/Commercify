package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.PaymentProvider;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.api.responses.CancelPaymentResponse;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("Payment Service Tests")
class PaymentServiceTests {

    @Nested
    @DisplayName("Payment Processing Tests")
    @ExtendWith(MockitoExtension.class)
    class PaymentProcessingTests {
        @Mock
        private PaymentRepository paymentRepository;

        @InjectMocks
        private PaymentService paymentService;

        private PaymentEntity mockPayment;

        @BeforeEach
        void setUp() {
            mockPayment = PaymentEntity.builder()
                    .id(1L)
                    .orderId(1L)
                    .status(PaymentStatus.PENDING)
                    .build();
        }

        @Test
        @DisplayName("Should return correct payment status")
        void getPaymentStatus_Success() {
            when(paymentRepository.findByOrderId(1L))
                    .thenReturn(Optional.of(mockPayment));

            PaymentStatus result = paymentService.getPaymentStatus(1L);

            assertThat(result).isEqualTo(PaymentStatus.PENDING);
        }

        @Test
        @DisplayName("Should return NOT_FOUND for non-existent payment")
        void getPaymentStatus_NotFound() {
            when(paymentRepository.findByOrderId(1L))
                    .thenReturn(Optional.empty());

            PaymentStatus result = paymentService.getPaymentStatus(1L);

            assertThat(result).isEqualTo(PaymentStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("Payment Cancellation Tests")
    @ExtendWith(MockitoExtension.class)
    class PaymentCancellationTests {
        @Mock
        private PaymentRepository paymentRepository;

        @Mock
        private StripeService stripeService;

        @InjectMocks
        private PaymentService paymentService;

        @Test
        @DisplayName("Should successfully cancel payment")
        void cancelPayment_Success() {
            PaymentEntity mockPayment = PaymentEntity.builder()
                    .id(1L)
                    .orderId(1L)
                    .status(PaymentStatus.PENDING)
                    .paymentProvider(PaymentProvider.STRIPE)
                    .build();

            when(paymentRepository.findByOrderId(1L))
                    .thenReturn(Optional.of(mockPayment));
            when(stripeService.cancelPayment(1L))
                    .thenReturn(new CancelPaymentResponse(true, "Payment cancelled successfully"));

            CancelPaymentResponse result = paymentService.cancelPayment(1L);

            assertTrue(result.success());
            assertEquals("Payment cancelled successfully", result.message());
        }

        @Test
        @DisplayName("Should return error for already cancelled payment")
        void cancelPayment_AlreadyCancelled_ReturnsError() {
            PaymentEntity mockPayment = PaymentEntity.builder()
                    .id(1L)
                    .orderId(1L)
                    .status(PaymentStatus.CANCELLED)
                    .build();

            when(paymentRepository.findByOrderId(1L))
                    .thenReturn(Optional.of(mockPayment));

            CancelPaymentResponse result = paymentService.cancelPayment(1L);

            assertFalse(result.success());
            assertEquals("Payment already canceled", result.message());
        }
    }
}