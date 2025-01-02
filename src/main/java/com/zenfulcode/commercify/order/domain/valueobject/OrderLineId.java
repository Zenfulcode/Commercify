package com.zenfulcode.commercify.order.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.Value;

import java.util.Objects;
import java.util.UUID;

@Value
@Embeddable
public class OrderLineId {
    String value;

    private OrderLineId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static OrderLineId generate() {
        return new OrderLineId(UUID.randomUUID().toString());
    }

    public static OrderLineId of(String value) {
        return new OrderLineId(value);
    }

    // Required by JPA
    protected OrderLineId() {
        this.value = null;
    }
}