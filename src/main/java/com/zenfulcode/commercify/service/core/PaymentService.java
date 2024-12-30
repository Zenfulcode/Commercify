package com.zenfulcode.commercify.service.core;

import com.zenfulcode.commercify.domain.enums.PaymentStatus;
import com.zenfulcode.commercify.web.dto.common.OrderDetailsDTO;
import com.zenfulcode.commercify.domain.model.Payment;
import com.zenfulcode.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.repository.PaymentRepository;
import com.zenfulcode.commercify.service.email.EmailService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final OrderService orderService;

    @Transactional
    public void handlePaymentStatusUpdate(Long orderId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));

        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(newStatus);
        paymentRepository.save(payment);

        // If payment is successful, send confirmation email
        if (newStatus == PaymentStatus.PAID && oldStatus != PaymentStatus.PAID) {
            try {
                orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

                // Get order details for email
                OrderDetailsDTO orderDetails = orderService.getOrderById(orderId);

                // Send confirmation email
                emailService.sendOrderConfirmation(orderDetails);

                log.info("Order confirmation email sent for order: {}", orderId);
            } catch (MessagingException e) {
                log.error("Failed to send order confirmation email for order {}: {}",
                        orderId, e.getMessage());
            }
        }
    }

    public PaymentStatus getPaymentStatus(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(Payment::getStatus)
                .orElse(PaymentStatus.NOT_FOUND);
    }
}