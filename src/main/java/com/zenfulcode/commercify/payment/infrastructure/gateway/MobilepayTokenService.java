package com.zenfulcode.commercify.payment.infrastructure.gateway;

import com.zenfulcode.commercify.payment.domain.exception.PaymentProcessingException;
import com.zenfulcode.commercify.payment.infrastructure.gateway.config.MobilepayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class MobilepayTokenService {
    private final RestTemplate restTemplate;
    private final ReentrantLock tokenLock = new ReentrantLock();
    private final MobilepayConfig config;

    private String accessToken;
    private Instant tokenExpiration;

    public MobilepayTokenService(RestTemplate restTemplate, MobilepayConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }
    
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
                MobilepayTokenResponse tokenResponse = requestNewAccessToken();
                accessToken = tokenResponse.accessToken();

                // Parse expires_on timestamp
                try {
                    long expiresOn = Long.parseLong(tokenResponse.expiresOn());
                    tokenExpiration = Instant.ofEpochSecond(expiresOn);
                    log.info("Access token refreshed, expires at: {}", tokenExpiration);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse token expiration timestamp", e);
                    // Fallback expiration
                    tokenExpiration = Instant.now().plusSeconds(3600);
                }
            }
        } finally {
            tokenLock.unlock();
        }
    }

    private MobilepayTokenResponse requestNewAccessToken() {
        try {
            HttpHeaders headers = createTokenRequestHeaders();

            ResponseEntity<MobilepayTokenResponse> response = restTemplate.exchange(
                    config.getApiUrl() + "/accesstoken/get",
                    HttpMethod.POST,
                    new HttpEntity<>(headers),
                    MobilepayTokenResponse.class
            );

            if (response.getBody() == null) {
                throw new PaymentProcessingException("No response from MobilePay token API", null);
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
        headers.set("client_id", config.getClientId());
        headers.set("client_secret", config.getClientSecret());
        headers.set("Ocp-Apim-Subscription-Key", config.getSubscriptionKey());
        headers.set("Merchant-Serial-Number", config.getMerchantId());
        headers.set("Vipps-System-Name", config.getSystemName());
        headers.set("Vipps-System-Version", "1.0");
        headers.set("Vipps-System-Plugin-Name", "commercify");
        headers.set("Vipps-System-Plugin-Version", "1.0");
        return headers;
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void scheduleTokenRefresh() {
        if (shouldRefreshToken()) {
            refreshAccessToken();
        }
    }
}