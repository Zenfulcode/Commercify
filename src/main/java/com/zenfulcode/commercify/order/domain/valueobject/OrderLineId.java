package com.zenfulcode.commercify.order.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLineId {
    @Column(name = "orderline_id")
    private String id;

    private OrderLineId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public static OrderLineId generate() {
        return new OrderLineId(UUID.randomUUID().toString());
    }

    public static OrderLineId of(String id) {
        return new OrderLineId(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderLineId that = (OrderLineId) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}