package com.zenfulcode.commercify.commercify.integration.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.zenfulcode.commercify.commercify.PaymentProvider;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;
import com.zenfulcode.commercify.commercify.dto.OrderLineDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderLineMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.commercify.exception.PaymentProcessingException;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
import com.zenfulcode.commercify.commercify.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderLineMapper orderLineMapper;
    private final ProductService productService;

    public PaymentResponse initiatePayment(PaymentRequest request) {
        try {
            OrderEntity order = orderRepository.findById(request.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

            // Create MobilePay payment request
            Map<String, Object> paymentRequest = createStripeRequest(order, request);

            // Call MobilePay API
            StripePaymentResponse stripeResponse = createStripePayment(paymentRequest);

            // Create and save payment entity
            PaymentEntity payment = PaymentEntity.builder()
                    .orderId(order.getId())
                    .totalAmount(order.getTotalAmount())
                    .paymentProvider(PaymentProvider.STRIPE)
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(request.paymentMethod()) // 'WALLET' or 'CARD'
                    .build();

            PaymentEntity savedPayment = paymentRepository.save(payment);

            return new PaymentResponse(
                    savedPayment.getId(),
                    savedPayment.getStatus(),
                    stripeResponse.redirectUrl()
            );
        } catch (Exception e) {
            log.error("Error creating Stripe payment", e);
            throw new PaymentProcessingException("Failed to create Stripe payment", e);
        }
    }

    private StripePaymentResponse createStripePayment(Map<String, Object> paymentRequest) {
        System.out.println("Payment request: " + paymentRequest);

        System.out.println("2 Payment request: " + paymentRequest.get("redirectUrl"));
        return new StripePaymentResponse((String) paymentRequest.get("redirectUrl"));
    }

    private Map<String, Object> createStripeRequest(OrderEntity order, PaymentRequest request) {
        validationPaymentRequest(request);

        Map<String, Object> paymentRequest = new HashMap<>();

        Session session;
        try {
            List<SessionCreateParams.LineItem> lineItems = createLineItems(order);

            System.out.println("Line items: " + lineItems.size());


            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                            .setReturnUrl(request.returnUrl())
                            .addAllLineItem(lineItems)
                            .build();

            session = Session.create(params);
            System.out.println("1 Payment request: " + session.getUrl());

            paymentRequest.put("redirectUrl", session.getUrl());

            return paymentRequest;
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    private List<SessionCreateParams.LineItem> createLineItems(OrderEntity order) {
        List<OrderLineDTO> orderLines = order.getOrderLines().stream().map(orderLineMapper).toList();

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        for (OrderLineDTO orderLine : orderLines) {
            ProductDTO product = productService.getProductById(orderLine.getProductId());

            System.out.println("Product: " + product);

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(orderLine.getQuantity().longValue())
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(order.getCurrency())
                                    .setUnitAmount(orderLine.getUnitPrice().longValue() * 100L)
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(product.getName())
                                                    .addImage(product.getImageUrl())
                                                    .setDescription(product.getDescription())
                                                    .build()
                                    ).build()
                    ).build();

            System.out.println("Line item: " + lineItem.getPriceData().getProduct());
            lineItems.add(lineItem);
        }

        return lineItems;
    }

    private void validationPaymentRequest(PaymentRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.paymentMethod() == null || request.paymentMethod().isEmpty()) {
            errors.add("Payment method is required");
        }

        if (!Objects.equals(request.paymentMethod(), "WALLET") && !Objects.equals(request.paymentMethod(), "CARD")) {
            errors.add("Invalid payment method");
        }

        if (request.returnUrl() == null || request.returnUrl().isEmpty()) {
            errors.add("Return URL is required");
        }

        if (!errors.isEmpty()) {
            throw new PaymentProcessingException("Invalid payment request: " + String.join(", ", errors), null);
        }
    }
}

record StripePaymentResponse(
        String redirectUrl
) {
}