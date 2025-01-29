package com.zenfulcode.commercify.payment.infrastructure.gateway.client;

import com.zenfulcode.commercify.payment.domain.exception.PaymentProcessingException;
import com.zenfulcode.commercify.payment.domain.exception.WebhookValidationException;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.model.WebhookConfig;
import com.zenfulcode.commercify.payment.domain.repository.WebhookConfigRepository;
import com.zenfulcode.commercify.payment.domain.valueobject.MobilepayWebhookRegistrationResponse;
import com.zenfulcode.commercify.payment.infrastructure.gateway.MobilepayCreatePaymentRequest;
import com.zenfulcode.commercify.payment.infrastructure.gateway.MobilepayPaymentResponse;
import com.zenfulcode.commercify.payment.infrastructure.gateway.MobilepayTokenService;
import com.zenfulcode.commercify.payment.infrastructure.gateway.config.MobilepayConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class MobilepayClient {
    private final WebhookConfigRepository webhookRepository;
    private final RestTemplate restTemplate;
    private final MobilepayTokenService tokenService;

    private final MobilepayConfig config;

    @Transactional
    public MobilepayPaymentResponse createPayment(MobilepayCreatePaymentRequest request) {
        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                    createPaymentRequest(request),
                    createHeaders()
            );

            ResponseEntity<MobilepayPaymentResponse> response = restTemplate.exchange(
                    config.getApiUrl() + "/epayment/v1/payments",
                    HttpMethod.POST,
                    entity,
                    MobilepayPaymentResponse.class
            );

            if (response.getBody() == null) {
                throw new PaymentProcessingException("Empty response from MobilePay", null);
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating MobilePay payment: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to create MobilePay payment", e);
        }
    }

    @Transactional
    public void validateWebhook(String contentSha256, String authorization, String date, String payload) {
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
            URI uri = new URI(config.getWebhookCallback());
            String expectedSignedString = String.format("POST\n%s\n%s;%s;%s", uri.getPath(), date, uri.getHost(), contentSha256);

            String secret = getWebhookSecret();
            byte[] secretByteArray = secret.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(secretByteArray, "HmacSHA256");
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
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
        } catch (InvalidKeyException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + tokenService.getAccessToken());
        headers.set("Ocp-Apim-Subscription-Key", config.getSubscriptionKey());
        headers.set("Merchant-Serial-Number", config.getMerchantId());
        headers.set("Vipps-System-Name", config.getSystemName());
        headers.set("Vipps-System-Version", "1.0");
        headers.set("Vipps-System-Plugin-Name", "commercify");
        headers.set("Vipps-System-Plugin-Version", "1.0");
        headers.set("Idempotency-Key", UUID.randomUUID().toString());
        return headers;
    }

    // TODO: Improve error handling for invalid currency not including (DKK, NOK, SEK and FIN)
    private Map<String, Object> createPaymentRequest(MobilepayCreatePaymentRequest request) {
        Map<String, Object> paymentRequest = new HashMap<>();

        // Amount
        final long value = Math.round(request.amount().getAmount().doubleValue() * 100);

        Map<String, Object> amount = new HashMap<>();
        amount.put("value", value);
        amount.put("currency", request.amount().getCurrency());
        paymentRequest.put("amount", amount);

        // Payment method
        Map<String, String> paymentMethod = new HashMap<>();
        paymentMethod.put("type", request.paymentMethod().name());
        paymentRequest.put("paymentMethod", paymentMethod);

        // Customer
        Map<String, String> customer = new HashMap<>();
        customer.put("phoneNumber", request.phoneNumber());
        paymentRequest.put("customer", customer);

        // Other fields
        paymentRequest.put("reference", "mp-" + request.orderId());
        paymentRequest.put("returnUrl", request.returnUrl() + "?orderId=" + request.orderId());
        paymentRequest.put("userFlow", "WEB_REDIRECT");

        return paymentRequest;
    }

    @Transactional
    public void registerWebhook(String callbackUrl) {
        HttpHeaders headers = createHeaders();

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
            ResponseEntity<MobilepayWebhookRegistrationResponse> response = restTemplate.exchange(
                    String.format("%s/webhooks/v1/webhooks", config.getApiUrl()),
                    HttpMethod.POST,
                    entity,
                    MobilepayWebhookRegistrationResponse.class
            );

            if (response.getBody() == null) {
                throw new WebhookValidationException("Empty response from MobilePay", null);
            }

            // Save or update webhook configuration
            saveOrUpdateWebhook(callbackUrl, response.getBody().secret());

            log.info("Webhook registration response: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error registering MobilePay webhook: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to register webhook", e);
        }
    }

    @Transactional
    protected void saveOrUpdateWebhook(String callbackUrl, String secret) {
        webhookRepository.findByProvider(PaymentProvider.MOBILEPAY)
                .ifPresentOrElse(
                        config -> {
                            config.setCallbackUrl(callbackUrl);
                            config.setSecret(secret);
                            webhookRepository.save(config);

                            log.info("Webhook updated successfully");
                        },
                        () -> {
                            WebhookConfig newConfig = WebhookConfig.builder()
                                    .provider(PaymentProvider.MOBILEPAY)
                                    .callbackUrl(callbackUrl)
                                    .secret(secret)
                                    .build();
                            webhookRepository.save(newConfig);

                            log.info("Webhook registered successfully");
                        }
                );
    }

    protected String getWebhookSecret() {
        return webhookRepository.findByProvider(PaymentProvider.MOBILEPAY)
                .map(WebhookConfig::getSecret)
                .orElseThrow(() -> new WebhookValidationException("Webhook secret not found", null));
    }

    @Transactional
    public void deleteWebhook(String webhookId) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    String.format("%s/webhooks/v1/webhooks/%s", config.getApiUrl(), webhookId),
                    HttpMethod.DELETE,
                    entity,
                    Object.class);

            log.info("Webhook deleted successfully: {}", webhookId);
        } catch (Exception e) {
            log.error("Error deleting MobilePay webhook: {}", e.getMessage());
            throw new RuntimeException("Failed to delete MobilePay webhook", e);
        }
    }

    @Transactional
    public Object getWebhooks() {
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    String.format("%s/webhooks/v1/webhooks", config.getApiUrl()),
                    HttpMethod.GET,
                    entity,
                    Object.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting MobilePay webhooks: {}", e.getMessage());
            throw new RuntimeException("Failed to get MobilePay webhooks", e);
        }
    }
}