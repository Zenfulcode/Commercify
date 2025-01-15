package com.zenfulcode.commercify.commercify.integration.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.zenfulcode.commercify.commercify.PaymentProvider;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.entity.VariantOptionEntity;
import com.zenfulcode.commercify.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.commercify.exception.PaymentProcessingException;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
import com.zenfulcode.commercify.commercify.service.product.ProductService;
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
            OrderEntity order = orderRepository.findById(request.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

            Session session = createCheckoutSession(order, request);

            PaymentEntity payment = PaymentEntity.builder()
                    .orderId(order.getId())
                    .totalAmount(order.getTotal())
                    .paymentProvider(PaymentProvider.STRIPE)
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(request.paymentMethod())
                    .build();

            PaymentEntity savedPayment = paymentRepository.save(payment);

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

    private Session createCheckoutSession(OrderEntity order, PaymentRequest request) throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (OrderLineEntity line : order.getOrderLines()) {
            ProductDTO product = productService.getProductById(line.getProductId());
            SessionCreateParams.LineItem.PriceData.ProductData.Builder productDataBuilder = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName(product.getName())
                    .setDescription(product.getDescription())
                    .addImage(product.getImageUrl());

            if (line.getProductVariant() != null) {
                StringBuilder variantInfo = new StringBuilder();
                for (VariantOptionEntity option : line.getProductVariant().getOptions()) {
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