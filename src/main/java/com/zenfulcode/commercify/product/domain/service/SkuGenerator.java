package com.zenfulcode.commercify.product.domain.service;

import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.valueobject.VariantSpecification;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SkuGenerator {
    private static final int SKU_LENGTH = 8;

    public String generateSku(Product product, VariantSpecification spec) {
        String basePrefix = product.getName()
                .substring(0, Math.min(3, product.getName().length()))
                .toUpperCase();

        String variantSuffix = spec.options().stream()
                .map(opt -> opt.value().substring(0, 1))
                .collect(Collectors.joining());

        String uniquePart = UUID.randomUUID()
                .toString()
                .substring(0, SKU_LENGTH - basePrefix.length() - variantSuffix.length());

        return basePrefix + uniquePart + variantSuffix;
    }
}
