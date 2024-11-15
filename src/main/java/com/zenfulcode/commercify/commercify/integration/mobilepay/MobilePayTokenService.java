package com.zenfulcode.commercify.commercify.integration.mobilepay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zenfulcode.commercify.commercify.exception.PaymentProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobilePayTokenService {
    private final RestTemplate restTemplate;
    private final ReentrantLock tokenLock = new ReentrantLock();

    @Value("${mobilepay.client-id}")
    private String clientId;

    @Value("${mobilepay.client-secret}")
    private String apiKey;

    @Value("${mobilepay.subscription-key}")
    private String subscriptionKey;

    @Value("${mobilepay.merchant-id}")
    private String merchantId;

    @Value("${mobilepay.system-name}")
    private String systemName;

    @Value("${mobilepay.api-url}")
    private String apiUrl;

    private String accessToken;
    private Instant tokenExpiration;

    public String getAccessToken() {
        if (shouldRefreshToken()) {
            refreshAccessToken();
        }
        return accessToken;
    }

    private boolean shouldRefreshToken() {
        return accessToken == null || tokenExpiration == null ||
                Instant.now().plusSeconds(60).isAfter(tokenExpiration);
    }

    private void refreshAccessToken() {
        tokenLock.lock();
        try {
            // Double-check after acquiring lock
            if (shouldRefreshToken()) {
                MobilePayTokenResponse tokenResponse = requestNewAccessToken();
                accessToken = tokenResponse.accessToken();

                // Parse expires_on timestamp for token expiration
                try {
                    long expiresOn = Long.parseLong(tokenResponse.expiresOn());
                    tokenExpiration = Instant.ofEpochSecond(expiresOn);
                    log.info("Access token refreshed, expires at: {}", tokenExpiration);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse token expiration timestamp", e);
                    // Fallback to a conservative expiration time
                    tokenExpiration = Instant.now().plusSeconds(3600);
                }
            }
        } finally {
            tokenLock.unlock();
        }
    }

    private MobilePayTokenResponse requestNewAccessToken() {
        try {
            HttpHeaders headers = createTokenRequestHeaders();

            ResponseEntity<MobilePayTokenResponse> response = restTemplate.exchange(
                    apiUrl + "/accesstoken/get",
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    MobilePayTokenResponse.class
            );

            if (response.getBody() == null) {
                throw new PaymentProcessingException("No response from MobilePay API", null);
            }

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to obtain access token", e);
            throw new PaymentProcessingException("Failed to obtain access token", e);
        }
    }

    private HttpHeaders createTokenRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("client_id", clientId);
        headers.set("client_secret", apiKey);
        headers.set("Ocp-Apim-Subscription-Key", subscriptionKey);
        headers.set("Merchant-Serial-Number", merchantId);
        headers.set("Vipps-System-Name", systemName);
        headers.set("Vipps-System-Version", "1.0");
        headers.set("Vipps-System-Plugin-Name", "commercify");
        headers.set("Vipps-System-Plugin-Version", "1.0");
        return headers;
    }

    // Refresh token periodically to ensure it's always valid
    @Scheduled(fixedRate = 3600000) // Every hour
    public void scheduleTokenRefresh() {
        if (shouldRefreshToken()) {
            refreshAccessToken();
        }
    }
}

record MobilePayTokenResponse(
        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        String expiresIn,

        @JsonProperty("ext_expires_in")
        String extExpiresIn,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_on")
        String expiresOn,

        @JsonProperty("not_before")
        String notBefore,

        @JsonProperty("resource")
        String resource
) {
}