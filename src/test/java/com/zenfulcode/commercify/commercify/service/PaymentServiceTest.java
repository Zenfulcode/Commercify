package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
import com.zenfulcode.commercify.commercify.service.email.EmailService;
import com.zenfulcode.commercify.commercify.service.order.OrderService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentEntity payment;
    private OrderEntity order;
    private OrderDetailsDTO orderDetails;

    @BeforeEach
    void setUp() {
        payment = PaymentEntity.builder()
                .id(1)
                .orderId(1)
                .status(PaymentStatus.PENDING)
                .totalAmount(199.99)
                .build();

        order = OrderEntity.builder()
                .id(1)
                .userId(1)
                .totalAmount(199.99)
                .build();

        orderDetails = new OrderDetailsDTO(null, null, null, null, null); // Simplified for testing
    }

    @Test
    @DisplayName("Should update payment status successfully")
    void handlePaymentStatusUpdate_Success() {
        when(paymentRepository.findByOrderId(1)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        paymentService.handlePaymentStatusUpdate(1, PaymentStatus.PAID);

        verify(paymentRepository).save(payment);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should send confirmation email when payment is successful")
    void handlePaymentStatusUpdate_SendsEmail() throws MessagingException {
        when(paymentRepository.findByOrderId(1)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderService.getOrderById(1)).thenReturn(orderDetails);

        paymentService.handlePaymentStatusUpdate(1, PaymentStatus.PAID);

        verify(emailService).sendOrderConfirmation(orderDetails);
    }

    @Test
    @DisplayName("Should not send email for non-successful payment status")
    void handlePaymentStatusUpdate_NoEmailForNonSuccess() throws MessagingException {
        when(paymentRepository.findByOrderId(1)).thenReturn(Optional.of(payment));

        paymentService.handlePaymentStatusUpdate(1, PaymentStatus.FAILED);

        verify(emailService, never()).sendOrderConfirmation(any());
    }

    @Test
    @DisplayName("Should handle payment not found")
    void handlePaymentStatusUpdate_PaymentNotFound() {
        when(paymentRepository.findByOrderId(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                paymentService.handlePaymentStatusUpdate(1, PaymentStatus.PAID));
    }

    @Test
    @DisplayName("Should handle email sending failure gracefully")
    void handlePaymentStatusUpdate_EmailFailure() throws MessagingException {
        when(paymentRepository.findByOrderId(1)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderService.getOrderById(1)).thenReturn(orderDetails);
        doThrow(new MessagingException("Failed to send email"))
                .when(emailService).sendOrderConfirmation(any());

        paymentService.handlePaymentStatusUpdate(1, PaymentStatus.PAID);

        verify(paymentRepository).save(payment);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should get payment status successfully")
    void getPaymentStatus_Success() {
        when(paymentRepository.findByOrderId(1)).thenReturn(Optional.of(payment));

        PaymentStatus status = paymentService.getPaymentStatus(1);

        assertThat(status).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Should return NOT_FOUND for non-existent payment")
    void getPaymentStatus_NotFound() {
        when(paymentRepository.findByOrderId(1)).thenReturn(Optional.empty());

        PaymentStatus status = paymentService.getPaymentStatus(1);

        assertThat(status).isEqualTo(PaymentStatus.NOT_FOUND);
    }
}