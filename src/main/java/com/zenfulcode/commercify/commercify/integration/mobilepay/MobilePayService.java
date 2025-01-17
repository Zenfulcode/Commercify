package com.zenfulcode.commercify.commercify.integration.mobilepay;

import com.zenfulcode.commercify.commercify.PaymentProvider;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.requests.WebhookPayload;
import com.zenfulcode.commercify.commercify.api.requests.products.PriceRequest;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.entity.WebhookConfigEntity;
import com.zenfulcode.commercify.commercify.exception.PaymentProcessingException;
import com.zenfulcode.commercify.commercify.integration.WebhookRegistrationResponse;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
import com.zenfulcode.commercify.commercify.repository.WebhookConfigRepository;
import com.zenfulcode.commercify.commercify.service.PaymentService;
import com.zenfulcode.commercify.commercify.service.email.EmailService;
import com.zenfulcode.commercify.commercify.service.order.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class MobilePayService extends PaymentService {
    private final MobilePayTokenService tokenService;

    private final OrderService orderService;
    private final PaymentRepository paymentRepository;

    private final RestTemplate restTemplate;
    private final WebhookConfigRepository webhookConfigRepository;

    @Value("${mobilepay.subscription-key}")
    private String subscriptionKey;

    @Value("${mobilepay.merchant-id}")
    private String merchantId;

    @Value("${mobilepay.system-name}")
    private String systemName;

    @Value("${mobilepay.api-url}")
    private String apiUrl;

    @Value("${commercify.host}")
    private String host;

    private static final String PROVIDER_NAME = "MOBILEPAY";

    public MobilePayService(PaymentRepository paymentRepository, EmailService emailService, OrderService orderService, MobilePayTokenService tokenService, OrderService orderService1, PaymentRepository paymentRepository1, RestTemplate restTemplate, WebhookConfigRepository webhookConfigRepository) {
        super(paymentRepository, emailService, orderService);
        this.tokenService = tokenService;
        this.orderService = orderService1;
        this.paymentRepository = paymentRepository1;
        this.restTemplate = restTemplate;
        this.webhookConfigRepository = webhookConfigRepository;
    }

    @Override
    public void capturePayment(Long paymentId, double captureAmount, boolean isPartialCapture) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Payment cannot captured");
        }

        OrderDetailsDTO order = orderService.getOrderById(payment.getOrderId());

        double capturingAmount = isPartialCapture ? captureAmount : payment.getTotalAmount();

        PriceRequest priceRequest = new PriceRequest(order.getOrder().getCurrency(), capturingAmount);

        // Capture payment
        if (payment.getMobilePayReference() != null) {
            capturePayment(payment.getMobilePayReference(), priceRequest);
        }

        // Update payment status
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
    }

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        try {
            OrderDetailsDTO orderDetails = orderService.getOrderById(request.orderId());
            // Create MobilePay payment request
            Map<String, Object> paymentRequest = createMobilePayRequest(orderDetails.getOrder(), request);

            // Call MobilePay API
            MobilePayCheckoutResponse mobilePayCheckoutResponse = createMobilePayPayment(paymentRequest);

            // Create and save payment entity
            PaymentEntity payment = PaymentEntity.builder()
                    .orderId(orderDetails.getOrder().getId())
                    .totalAmount(orderDetails.getOrder().getTotal())
                    .paymentProvider(PaymentProvider.MOBILEPAY)
                    .status(PaymentStatus.PENDING)
                    .paymentMethod(request.paymentMethod()) // 'WALLET' or 'CARD'
                    .mobilePayReference(mobilePayCheckoutResponse.reference())
                    .build();

            PaymentEntity savedPayment = paymentRepository.save(payment);

            return new PaymentResponse(
                    savedPayment.getId(),
                    savedPayment.getStatus(),
                    mobilePayCheckoutResponse.redirectUrl()
            );
        } catch (Exception e) {
            log.error("Error creating MobilePay payment", e);
            throw new PaymentProcessingException("Failed to create MobilePay payment", e);
        }
    }

    @Transactional
    public void handlePaymentCallback(WebhookPayload payload) {
        PaymentEntity payment = paymentRepository.findByMobilePayReference(payload.reference())
                .orElseThrow(() -> new PaymentProcessingException("Payment not found", null));

        PaymentStatus newStatus = mapMobilePayStatus(payload.name());

        // Update payment status and trigger confirmation email if needed
        handlePaymentStatusUpdate(payment.getOrderId(), newStatus);
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
    private MobilePayCheckoutResponse createMobilePayPayment(Map<String, Object> request) {
        HttpHeaders headers = mobilePayRequestHeaders();
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<MobilePayCheckoutResponse> response = restTemplate.exchange(
                    apiUrl + "/epayment/v1/payments",
                    HttpMethod.POST,
                    entity,
                    MobilePayCheckoutResponse.class
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

    public Map<String, Object> createMobilePayRequest(OrderDTO order, PaymentRequest request) {
        validationPaymentRequest(request);

        Map<String, Object> paymentRequest = new HashMap<>();

        // Amount object
        Map<String, Object> amount = new HashMap<>();
        String value = String.valueOf(Math.round(order.getTotal() * 100)); // Convert to minor units
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
        String reference = String.join("-", merchantId, systemName, String.valueOf(order.getId()), value);
        paymentRequest.put("reference", reference);
        paymentRequest.put("returnUrl", request.returnUrl() + "?orderId=" + order.getId());
        paymentRequest.put("userFlow", "WEB_REDIRECT");
        paymentRequest.put("paymentDescription", "Order Number #" + order.getId());

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
            case "ABORTED", "CANCELLED" -> PaymentStatus.CANCELLED;
            case "EXPIRED" -> PaymentStatus.EXPIRED;
            case "TERMINATED" -> PaymentStatus.TERMINATED;
            case "CAPTURED" -> PaymentStatus.CAPTURED;
            case "REFUNDED" -> PaymentStatus.REFUNDED;
            default -> throw new PaymentProcessingException("Unknown MobilePay status: " + status, null);
        };
    }

    @Transactional
    public void registerWebhooks(String callbackUrl) {
        HttpHeaders headers = mobilePayRequestHeaders();

        Map<String, Object> request = new HashMap<>();
        request.put("url", callbackUrl);
        request.put("events", new String[]{
                "epayments.payment.aborted.v1",
                "epayments.payment.expired.v1",
                "epayments.payment.cancelled.v1",
                "epayments.payment.captured.v1",
                "epayments.payment.refunded.v1",
                "epayments.payment.authorized.v1"
        });

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<WebhookRegistrationResponse> response = restTemplate.exchange(
                    apiUrl + "/webhooks/v1/webhooks",
                    HttpMethod.POST,
                    entity,
                    WebhookRegistrationResponse.class
            );

            if (response.getBody() == null) {
                throw new PaymentProcessingException("No response from MobilePay API", null);
            }

            // Save or update webhook configuration
            webhookConfigRepository.findByProvider(PROVIDER_NAME)
                    .ifPresentOrElse(
                            config -> {
                                config.setWebhookUrl(callbackUrl);
                                config.setWebhookSecret(response.getBody().secret());
                                webhookConfigRepository.save(config);

                                log.info("Webhook updated successfully");
                            },
                            () -> {
                                WebhookConfigEntity newConfig = WebhookConfigEntity.builder()
                                        .provider(PROVIDER_NAME)
                                        .webhookUrl(callbackUrl)
                                        .webhookSecret(response.getBody().secret())
                                        .build();
                                webhookConfigRepository.save(newConfig);

                                log.info("Webhook registered successfully");
                            }
                    );

        } catch (Exception e) {
            log.error("Error registering MobilePay webhooks: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to register MobilePay webhooks", e);
        }
    }

    @Transactional(readOnly = true)
    public void authenticateRequest(String date, String contentSha256, String authorization, String payload, HttpServletRequest request) {
        try {
//            Verify content
            log.info("Verifying content");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            String encodedHash = Base64.getEncoder().encodeToString(hash);

            if (!encodedHash.equals(contentSha256)) {
                throw new SecurityException("Hash mismatch");
            }

            log.info("Content verified");

//            Verify signature
            log.info("Verifying signature");
            String path = request.getRequestURI();
            URI uri = new URI(host + path);

            String expectedSignedString = String.format("POST\n%s\n%s;%s;%s", path, date, uri.getHost(), encodedHash);

            Mac hmacSha256 = Mac.getInstance("HmacSHA256");

            CompletableFuture<byte[]> secretByteArray = getWebhookSecret().thenApply(s -> s.getBytes(StandardCharsets.UTF_8));

            SecretKeySpec secretKey = new SecretKeySpec(secretByteArray.get(), "HmacSHA256");
            hmacSha256.init(secretKey);

            byte[] hmacSha256Bytes = hmacSha256.doFinal(expectedSignedString.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Base64.getEncoder().encodeToString(hmacSha256Bytes);
            String expectedAuthorization = String.format("HMAC-SHA256 SignedHeaders=x-ms-date;host;x-ms-content-sha256&Signature=%s", expectedSignature);

            if (!authorization.equals(expectedAuthorization)) {
                throw new SecurityException("Signature mismatch");
            }

            log.info("Signature verified");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        } catch (InvalidKeyException | URISyntaxException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteWebhook(String id) {
        HttpHeaders headers = mobilePayRequestHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    apiUrl + "/webhooks/v1/webhooks/" + id,
                    HttpMethod.DELETE,
                    entity,
                    Object.class);

            log.info("Webhook deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting MobilePay webhook: {}", e.getMessage());
            throw new RuntimeException("Failed to delete MobilePay webhook", e);
        }
    }

    public Object getWebhooks() {
        HttpHeaders headers = mobilePayRequestHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    apiUrl + "/webhooks/v1/webhooks",
                    HttpMethod.GET,
                    entity,
                    Object.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting MobilePay webhooks: {}", e.getMessage());
            throw new RuntimeException("Failed to get MobilePay webhooks", e);
        }
    }

    @Async
    protected CompletableFuture<String> getWebhookSecret() {
        try {
            final String secret = webhookConfigRepository.findByProvider(PROVIDER_NAME)
                    .map(WebhookConfigEntity::getWebhookSecret)
                    .orElseThrow(() -> new PaymentProcessingException("Webhook secret not found", null));

            return CompletableFuture.completedFuture(secret);
        } catch (Exception e) {
            log.error("Error getting webhook secret: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    public void capturePayment(String mobilePayReference, PriceRequest captureAmount) {
        paymentRepository.findByMobilePayReference(mobilePayReference)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found", null));

        HttpHeaders headers = mobilePayRequestHeaders();

        Map<String, Object> request = new HashMap<>();
        request.put("modificationAmount", new MobilePayPrice(Math.round(captureAmount.amount() * 100), captureAmount.currency()));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            restTemplate.exchange(
                    apiUrl + "/epayment/v1/payments/" + mobilePayReference + "/capture",
                    HttpMethod.POST,
                    entity,
                    Object.class);
        } catch (Exception e) {
            log.error("Error capturing MobilePay payment: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to capture MobilePay payment", e);
        }
    }
}

record MobilePayPrice(
        long value,
        String currency
) {
}

record MobilePayCheckoutResponse(
        String redirectUrl,
        String reference
) {
}