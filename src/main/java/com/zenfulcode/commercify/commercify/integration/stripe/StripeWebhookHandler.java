package com.zenfulcode.commercify.commercify.integration.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.commercify.exception.PaymentProcessingException;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
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

        OrderEntity order = orderRepository.findById(Long.parseLong(orderId))
                .orElseThrow(() -> new OrderNotFoundException(Long.parseLong(orderId)));

        // Update order status
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Update payment status
        PaymentEntity payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
    }
}