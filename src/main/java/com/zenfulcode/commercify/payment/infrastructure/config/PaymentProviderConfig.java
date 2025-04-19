package com.zenfulcode.commercify.payment.infrastructure.config;

import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.service.PaymentProviderFactory;
import com.zenfulcode.commercify.payment.domain.service.provider.MobilepayProviderService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PaymentProviderConfig {
    private final PaymentProviderFactory providerFactory;
    private final MobilepayProviderService mobilePayService;

    @PostConstruct
    public void registerProviders() {
        providerFactory.registerProvider(PaymentProvider.MOBILEPAY, mobilePayService);
    }
}