package com.zenfulcode.commercify.service.integration.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.zenfulcode.commercify.domain.enums.OrderStatus;
import com.zenfulcode.commercify.domain.enums.PaymentStatus;
import com.zenfulcode.commercify.domain.model.Order;
import com.zenfulcode.commercify.domain.model.Payment;
import com.zenfulcode.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.exception.PaymentProcessingException;
import com.zenfulcode.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookHandler {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final String stripeWebhookSecret;

    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            throw new PaymentProcessingException("Invalid signature", e);
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().get();
            handleSuccessfulPayment(session);
        }
    }

    private void handleSuccessfulPayment(Session session) {
        String orderId = session.getMetadata().get("orderId");
        if (orderId == null) {
            log.error("No order ID in session metadata");
            return;
        }

        Order order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new OrderNotFoundException(Long.parseLong(orderId)));

        // Update order status
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // Update payment status
        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
    }
}