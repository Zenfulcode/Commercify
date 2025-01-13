package com.zenfulcode.commercify.payment.domain.service;

import com.zenfulcode.commercify.payment.domain.exception.PaymentProviderNotFoundException;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentProviderConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentProviderFactory {
    private final Map<PaymentProvider, PaymentProviderService> providerServices = new HashMap<>();

    public PaymentProviderService getProvider(PaymentProvider provider) {
        PaymentProviderService service = providerServices.get(provider);
        if (service == null) {
            throw new PaymentProviderNotFoundException("Payment provider not found: " + provider);
        }
        return service;
    }

    public List<PaymentProviderConfig> getAvailableProviders() {
        return providerServices.values().stream()
                .map(PaymentProviderService::getProviderConfig)
                .filter(PaymentProviderConfig::isActive)
                .collect(Collectors.toList());
    }
}