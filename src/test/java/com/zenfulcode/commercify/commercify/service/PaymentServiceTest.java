package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
import com.zenfulcode.commercify.commercify.service.email.EmailService;
import com.zenfulcode.commercify.commercify.service.order.OrderService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private OrderService orderService;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, emailService, orderService);
    }

    @Nested
    @DisplayName("Payment Status Update Tests")
    class PaymentStatusUpdateTests {

        @Test
        @DisplayName("Should successfully update payment status and send confirmation email when payment is successful")
        void shouldUpdateStatusAndSendEmailOnSuccessfulPayment() throws MessagingException {
            // Arrange
            Long orderId = 1L;
            PaymentEntity payment = PaymentEntity.builder()
                    .id(1L)
                    .orderId(orderId)
                    .status(PaymentStatus.PENDING)
                    .build();

            OrderDetailsDTO orderDetails = new OrderDetailsDTO();
            orderDetails.setOrder(new OrderDTO());

            when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
            when(orderService.getOrderById(orderId)).thenReturn(orderDetails);

            // Act
            paymentService.handlePaymentStatusUpdate(orderId, PaymentStatus.PAID);

            // Assert
            verify(paymentRepository).save(payment);
            verify(orderService).updateOrderStatus(orderId, PaymentStatus.PAID);
            verify(emailService).sendOrderConfirmation(orderDetails);
            verify(emailService).sendNewOrderNotification(orderDetails);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        }

        @Test
        @DisplayName("Should throw exception when payment is not found")
        void shouldThrowExceptionWhenPaymentNotFound() {
            // Arrange
            Long orderId = 1L;
            when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> paymentService.handlePaymentStatusUpdate(orderId, PaymentStatus.PAID));
        }

//        @Test
//        @DisplayName("Should not send email for non-successful payment status updates")
//        void shouldNotSendEmailForNonSuccessfulPayments() {
//            // Arrange
//            Long orderId = 1L;
//            PaymentEntity payment = PaymentEntity.builder()
//                    .id(1L)
//                    .orderId(orderId)
//                    .status(PaymentStatus.PENDING)
//                    .build();
//
//            when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
//
//            // Act
//            paymentService.handlePaymentStatusUpdate(orderId, PaymentStatus.CANCELLED);
//
//            // Assert
//            verify(paymentRepository).save(payment);
//            verify(orderService).updateOrderStatus(orderId, PaymentStatus.CANCELLED);
//            verify(emailService, never()).sendOrderConfirmation(any());
//            verify(emailService, never()).sendNewOrderNotification(any());
//            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
//        }
    }

    @Nested
    @DisplayName("Get Payment Status Tests")
    class GetPaymentStatusTests {

        @Test
        @DisplayName("Should return correct payment status when payment exists")
        void shouldReturnCorrectPaymentStatus() {
            // Arrange
            Long orderId = 1L;
            PaymentEntity payment = PaymentEntity.builder()
                    .id(1L)
                    .orderId(orderId)
                    .status(PaymentStatus.PAID)
                    .build();

            when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

            // Act
            PaymentStatus status = paymentService.getPaymentStatus(orderId);

            // Assert
            assertThat(status).isEqualTo(PaymentStatus.PAID);
        }

        @Test
        @DisplayName("Should return NOT_FOUND status when payment doesn't exist")
        void shouldReturnNotFoundStatusWhenPaymentDoesntExist() {
            // Arrange
            Long orderId = 1L;
            when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            // Act
            PaymentStatus status = paymentService.getPaymentStatus(orderId);

            // Assert
            assertThat(status).isEqualTo(PaymentStatus.NOT_FOUND);
        }
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException when attempting to capture payment")
    void shouldThrowExceptionWhenCapturingPayment() {
        assertThrows(UnsupportedOperationException.class,
                () -> paymentService.capturePayment(1L, 100.0, false));
    }
}