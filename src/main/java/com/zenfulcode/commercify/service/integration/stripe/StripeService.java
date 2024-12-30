package com.zenfulcode.commercify.service.integration.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.zenfulcode.commercify.domain.enums.PaymentProvider;
import com.zenfulcode.commercify.domain.enums.PaymentStatus;
import com.zenfulcode.commercify.web.dto.request.payment.PaymentRequest;
import com.zenfulcode.commercify.web.dto.response.payment.PaymentResponse;
import com.zenfulcode.commercify.web.dto.common.ProductDTO;
import com.zenfulcode.commercify.domain.model.Order;
import com.zenfulcode.commercify.domain.model.OrderLine;
import com.zenfulcode.commercify.domain.model.Payment;
import com.zenfulcode.commercify.domain.model.VariantOption;
import com.zenfulcode.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.exception.PaymentProcessingException;
import com.zenfulcode.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.repository.PaymentRepository;
import com.zenfulcode.commercify.service.core.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductService productService;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        try {
            Order order = orderRepository.findById(request.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

            Session session = createCheckoutSession(order, request);

            Payment payment = Payment.builder()
                    .orderId(order.getId())
                    .totalAmount(order.getTotalAmount())
                    .paymentProvider(PaymentProvider.STRIPE)
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(request.paymentMethod())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            return new PaymentResponse(
                    savedPayment.getId(),
                    savedPayment.getStatus(),
                    session.getUrl()
            );
        } catch (Exception e) {
            log.error("Error creating Stripe checkout session", e);
            throw new PaymentProcessingException("Failed to create Stripe checkout session", e);
        }
    }

    private Session createCheckoutSession(Order order, PaymentRequest request) throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (OrderLine line : order.getOrderLines()) {
            ProductDTO product = productService.getProductById(line.getProductId());
            SessionCreateParams.LineItem.PriceData.ProductData.Builder productDataBuilder = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName(product.getName())
                    .setDescription(product.getDescription())
                    .addImage(product.getImageUrl());

            if (line.getProductVariant() != null) {
                StringBuilder variantInfo = new StringBuilder();
                for (VariantOption option : line.getProductVariant().getOptions()) {
                    variantInfo.append(option.getName())
                            .append(": ")
                            .append(option.getValue())
                            .append(", ");
                }
                if (!variantInfo.isEmpty()) {
                    variantInfo.setLength(variantInfo.length() - 2);
                    productDataBuilder.putMetadata("variant", variantInfo.toString());
                }
            }

            lineItems.add(SessionCreateParams.LineItem.builder()
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(order.getCurrency().toLowerCase())
                            .setUnitAmount((long) (line.getUnitPrice() * 100))
                            .setProductData(productDataBuilder.build())
                            .build())
                    .setQuantity((long) line.getQuantity())
                    .build());
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(request.returnUrl() + "?success=true&orderId=" + order.getId())
                .setCancelUrl(request.returnUrl() + "?success=false&orderId=" + order.getId())
                .putMetadata("orderId", order.getId().toString())
                .addAllLineItem(lineItems)
                .build();

        return Session.create(params);
    }
}