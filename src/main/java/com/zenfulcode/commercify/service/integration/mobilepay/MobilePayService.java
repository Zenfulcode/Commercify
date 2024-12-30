package com.zenfulcode.commercify.service.integration.mobilepay;

import com.zenfulcode.commercify.domain.enums.PaymentProvider;
import com.zenfulcode.commercify.domain.enums.PaymentStatus;
import com.zenfulcode.commercify.web.dto.request.payment.PaymentRequest;
import com.zenfulcode.commercify.web.dto.response.payment.PaymentResponse;
import com.zenfulcode.commercify.domain.model.Order;
import com.zenfulcode.commercify.domain.model.Payment;
import com.zenfulcode.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.exception.PaymentProcessingException;
import com.zenfulcode.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.repository.PaymentRepository;
import com.zenfulcode.commercify.service.core.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobilePayService {
    private final PaymentService paymentService;
    private final MobilePayTokenService tokenService;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    private final RestTemplate restTemplate;

    @Value("${mobilepay.subscription-key}")
    private String subscriptionKey;

    @Value("${mobilepay.merchant-id}")
    private String merchantId;

    @Value("${mobilepay.system-name}")
    private String systemName;

    @Value("${mobilepay.api-url}")
    private String apiUrl;


    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        try {
            Order order = orderRepository.findById(request.orderId())
                    .orElseThrow(() -> new OrderNotFoundException(request.orderId()));

            // Create MobilePay payment request
            Map<String, Object> paymentRequest = createMobilePayRequest(order, request);

            // Call MobilePay API
            MobilePayResponse mobilePayResponse = createMobilePayPayment(paymentRequest);

            // Create and save payment entity
            Payment payment = Payment.builder()
                    .orderId(order.getId())
                    .totalAmount(order.getTotalAmount())
                    .paymentProvider(PaymentProvider.MOBILEPAY)
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(request.paymentMethod()) // 'WALLET' or 'CARD'
                    .mobilePayReference(mobilePayResponse.reference())
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            return new PaymentResponse(
                    savedPayment.getId(),
                    savedPayment.getStatus(),
                    mobilePayResponse.redirectUrl()
            );
        } catch (Exception e) {
            log.error("Error creating MobilePay payment", e);
            throw new PaymentProcessingException("Failed to create MobilePay payment", e);
        }
    }

    @Transactional
    public void handlePaymentCallback(String paymentReference, String status) {
        Payment payment = paymentRepository.findByMobilePayReference(paymentReference)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found", null));

        PaymentStatus newStatus = mapMobilePayStatus(status);

        // Update payment status and trigger confirmation email if needed
        paymentService.handlePaymentStatusUpdate(payment.getOrderId(), newStatus);
    }

    private HttpHeaders mobilePayRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + tokenService.getAccessToken());
        headers.set("Ocp-Apim-Subscription-Key", subscriptionKey);
        headers.set("Merchant-Serial-Number", merchantId);
        headers.set("Vipps-System-Name", systemName);
        headers.set("Vipps-System-Version", "1.0");
        headers.set("Vipps-System-Plugin-Name", "commercify");
        headers.set("Vipps-System-Plugin-Version", "1.0");
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        return headers;
    }

    @Retryable(
            notRecoverable = {PaymentProcessingException.class},
            backoff = @Backoff(delay = 1000)
    )
    private MobilePayResponse createMobilePayPayment(Map<String, Object> request) {
        HttpHeaders headers = mobilePayRequestHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<MobilePayResponse> response = restTemplate.exchange(
                    apiUrl + "/epayment/v1/payments",
                    HttpMethod.POST,
                    entity,
                    MobilePayResponse.class
            );

            if (response.getBody() == null) {
                throw new PaymentProcessingException("No response from MobilePay API", null);
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating MobilePay payment: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to create MobilePay payment", e);
        }
    }

    private Map<String, Object> createMobilePayRequest(Order order, PaymentRequest request) {
        validationPaymentRequest(request);

        Map<String, Object> paymentRequest = new HashMap<>();

        // Amount object
        Map<String, Object> amount = new HashMap<>();
        String value = String.valueOf(Math.round(order.getTotalAmount() * 100)); // Convert to minor units
        amount.put("value", value); // Convert to minor units
        amount.put("currency", "DKK");
        paymentRequest.put("amount", amount);

        // Payment method object
        Map<String, String> paymentMethod = new HashMap<>();
        paymentMethod.put("type", request.paymentMethod());
        paymentRequest.put("paymentMethod", paymentMethod);

        // Customer object
        Map<String, String> customer = new HashMap<>();
        customer.put("phoneNumber", request.phoneNumber());
        paymentRequest.put("customer", customer);

        // Other fields
        String reference = systemName + "-order-" + order.getId().toString() + "-" + value;
        paymentRequest.put("reference", reference);
        paymentRequest.put("returnUrl", request.returnUrl() + "?orderId=" + order.getId());
        paymentRequest.put("userFlow", "WEB_REDIRECT");
        paymentRequest.put("paymentDescription", "Order #" + order.getId());

        return paymentRequest;
    }

    private void validationPaymentRequest(PaymentRequest request) {
        List<String> errors = new ArrayList<>();

        if (!Objects.equals(request.currency(), "DKK")) {
            errors.add("Currency must be DKK");
        }

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

    private PaymentStatus mapMobilePayStatus(String status) {
        return switch (status.toUpperCase()) {
            case "CREATED" -> PaymentStatus.PENDING;
            case "AUTHORIZED" -> PaymentStatus.PAID;
            case "ABORTED" -> PaymentStatus.CANCELLED;
            case "EXPIRED" -> PaymentStatus.EXPIRED;
            case "TERMINATED" -> PaymentStatus.TERMINATED;
            default -> throw new PaymentProcessingException("Unknown MobilePay status: " + status, null);
        };
    }
}

record MobilePayResponse(
        String redirectUrl,
        String reference
) {
}

