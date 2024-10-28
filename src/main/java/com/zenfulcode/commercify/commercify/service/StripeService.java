package com.zenfulcode.commercify.commercify.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Product;
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

        List<OrderLineDTO> orderLineDTOS = order.getOrderLines();

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl(paymentRequest.successUrl())
                        .setCancelUrl(paymentRequest.cancelUrl())
                        .setCurrency(paymentRequest.currency())
                        .addAllLineItem(orderLineDTOS.stream().map(ol -> {
                            Long quantity = Long.valueOf(ol.getQuantity());
                            if (quantity <= 0)
                                throw new RuntimeException("Invalid quantity for order line");

                            SessionCreateParams.LineItem.Builder lineItem = SessionCreateParams.LineItem.builder()
                                    .setQuantity(quantity);

                            try {
                                Product product = Product.retrieve(ol.getStripeProductId());
                                lineItem.setPrice(product.getDefaultPrice());
                            } catch (StripeException e) {
                                throw new RuntimeException(e);
                            }

                            return lineItem.build();
                        }).toList())
                        .putMetadata("orderId", paymentRequest.orderId().toString())
                        .build();
        try {
            // Create the PaymentIntent
            Session session = Session.create(params);

            // Save the payment in our local database with status "PENDING"
            PaymentEntity payment = PaymentEntity.builder()
                    .orderId(paymentRequest.orderId())
                    .stripePaymentIntent(session.getPaymentIntent())
                    .paymentProvider(PaymentProvider.STRIPE)
                    .status(PaymentStatus.PAID)
                    .build();
            paymentRepository.save(payment);

            System.out.println("Payment session created: " + session.getUrl());

            orderService.updateOrderStatus(paymentRequest.orderId(), OrderStatus.CONFIRMED);

            // Return the payment intent's client secret for client-side confirmation
            return new PaymentResponse(payment.getPaymentId(), payment.getStatus(), session.getUrl());
        } catch (StripeException e) {
            System.out.println("Error processing payment: " + e.getMessage());
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

