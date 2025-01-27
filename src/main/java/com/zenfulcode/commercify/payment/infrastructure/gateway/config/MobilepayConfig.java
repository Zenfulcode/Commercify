package com.zenfulcode.commercify.payment.infrastructure.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "integration.payments.mobilepay")
public class MobilepayConfig {
    private String apiKey;
    private String merchantId;
    private String apiUrl;
    private String clientId;
    private String clientSecret;
    private String subscriptionKey;
    private String systemName;
    private String host;
}
