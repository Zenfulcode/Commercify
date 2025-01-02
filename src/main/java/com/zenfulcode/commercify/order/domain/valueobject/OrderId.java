package com.zenfulcode.commercify.order.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
@Embeddable
public class OrderId {
    String value;

    private OrderId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID().toString());
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    // Required by JPA
    protected OrderId() {
        this.value = null;
    }
}