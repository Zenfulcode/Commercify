package com.zenfulcode.commercify.product.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VariantId extends ProductId {
    private String id;

    private VariantId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public static VariantId generate() {
        return new VariantId(UUID.randomUUID().toString());
    }

    public static VariantId of(String id) {
        return new VariantId(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariantId that = (VariantId) o;
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