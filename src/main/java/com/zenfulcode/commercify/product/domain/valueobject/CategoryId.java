package com.zenfulcode.commercify.product.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
@Embeddable
public class CategoryId {
    String value;

    private CategoryId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID().toString());
    }

    public static CategoryId of(String value) {
        return new CategoryId(value);
    }

    // Required by JPA
    protected CategoryId() {
        this.value = null;
    }
}
