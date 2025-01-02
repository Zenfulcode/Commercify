package com.zenfulcode.commercify.product.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
@Embeddable
public class ProductId {
    String value;

    private ProductId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID().toString());
    }

    public static ProductId of(String value) {
        return new ProductId(value);
    }

    // Required by JPA
    protected ProductId() {
        this.value = null;
    }
}
