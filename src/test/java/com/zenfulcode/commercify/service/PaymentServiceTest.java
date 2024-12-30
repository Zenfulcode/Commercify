package com.zenfulcode.commercify.service;

import com.zenfulcode.commercify.domain.enums.PaymentStatus;
import com.zenfulcode.commercify.web.dto.common.OrderDetailsDTO;
import com.zenfulcode.commercify.domain.model.Order;
import com.zenfulcode.commercify.domain.model.Payment;
import com.zenfulcode.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.repository.PaymentRepository;
import com.zenfulcode.commercify.service.core.PaymentService;
import com.zenfulcode.commercify.service.email.EmailService;
import com.zenfulcode.commercify.service.core.OrderService;
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

    private Payment payment;
    private Order order;
    private OrderDetailsDTO orderDetails;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .status(PaymentStatus.PENDING)
                .totalAmount(199.99)
                .build();

        order = Order.builder()
                .id(1L)
                .userId(1L)
                .totalAmount(199.99)
                .build();

        orderDetails = new OrderDetailsDTO(null, null, null, null, null); // Simplified for testing
    }

    @Test
    @DisplayName("Should update payment status successfully")
    void handlePaymentStatusUpdate_Success() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        paymentService.handlePaymentStatusUpdate(1L, PaymentStatus.PAID);

        verify(paymentRepository).save(payment);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should send confirmation email when payment is successful")
    void handlePaymentStatusUpdate_SendsEmail() throws MessagingException {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderService.getOrderById(1L)).thenReturn(orderDetails);

        paymentService.handlePaymentStatusUpdate(1L, PaymentStatus.PAID);

        verify(emailService).sendOrderConfirmation(orderDetails);
    }

    @Test
    @DisplayName("Should not send email for non-successful payment status")
    void handlePaymentStatusUpdate_NoEmailForNonSuccess() throws MessagingException {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));

        paymentService.handlePaymentStatusUpdate(1L, PaymentStatus.FAILED);

        verify(emailService, never()).sendOrderConfirmation(any());
    }

    @Test
    @DisplayName("Should handle payment not found")
    void handlePaymentStatusUpdate_PaymentNotFound() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                paymentService.handlePaymentStatusUpdate(1L, PaymentStatus.PAID));
    }

    @Test
    @DisplayName("Should handle email sending failure gracefully")
    void handlePaymentStatusUpdate_EmailFailure() throws MessagingException {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderService.getOrderById(1L)).thenReturn(orderDetails);
        doThrow(new MessagingException("Failed to send email"))
                .when(emailService).sendOrderConfirmation(any());

        paymentService.handlePaymentStatusUpdate(1L, PaymentStatus.PAID);

        verify(paymentRepository).save(payment);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("Should get payment status successfully")
    void getPaymentStatus_Success() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));

        PaymentStatus status = paymentService.getPaymentStatus(1L);

        assertThat(status).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Should return NOT_FOUND for non-existent payment")
    void getPaymentStatus_NotFound() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        PaymentStatus status = paymentService.getPaymentStatus(1L);

        assertThat(status).isEqualTo(PaymentStatus.NOT_FOUND);
    }
}