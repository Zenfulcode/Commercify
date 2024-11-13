package com.zenfulcode.commercify.commercify.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.PaymentProvider;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.responses.CancelPaymentResponse;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.dto.OrderLineDTO;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StripeService {
    private final PaymentRepository paymentRepository;

    public PaymentResponse checkoutSession(PaymentRequest paymentRequest, OrderService orderService) {
        OrderDetailsDTO order = orderService.getOrderById(paymentRequest.orderId());
        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        List<OrderLineDTO> orderLines = order.getOrderLines();

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(paymentRequest.successUrl())
                    .setCancelUrl(paymentRequest.cancelUrl())
                    .setCurrency(order.getOrder().getCurrency())
                    .addAllLineItem(orderLines.stream().map(ol -> {
                        Long quantity = Long.valueOf(ol.getQuantity());
                        if (quantity <= 0)
                            throw new RuntimeException("Invalid quantity for order line");

                        // Use variant's stripe price ID if available, otherwise use product's
                        String stripePriceId = ol.getVariant() != null ?
                                ol.getVariant().getStripePriceId() :
                                ol.getProduct().getStripePriceId();

                        return SessionCreateParams.LineItem.builder()
                                .setPrice(stripePriceId)
                                .setQuantity(quantity)
                                .build();
                    }).toList())
                    .putMetadata("orderId", paymentRequest.orderId().toString())
                    .build();

            Session session = Session.create(params);

            PaymentEntity payment = PaymentEntity.builder()
                    .orderId(paymentRequest.orderId())
                    .stripePaymentIntent(session.getPaymentIntent())
                    .paymentProvider(PaymentProvider.STRIPE)
                    .status(PaymentStatus.PENDING)
                    .totalAmount(order.getOrder().getTotalAmount())
                    .build();

            paymentRepository.save(payment);
            orderService.updateOrderStatus(paymentRequest.orderId(), OrderStatus.CONFIRMED);

            return new PaymentResponse(payment.getId(), payment.getStatus(), session.getUrl());
        } catch (StripeException e) {
            return PaymentResponse.FailedPayment();
        }
    }

    public CancelPaymentResponse cancelPayment(Long paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + paymentId));

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentIntent());
            paymentIntent.cancel();
        } catch (StripeException e) {
            return new CancelPaymentResponse(false, e.getMessage());
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);

        return new CancelPaymentResponse(true, "Payment cancelled successfully");
    }
}

