package com.zenfulcode.commercify.product.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "variant_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VariantOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductVariant variant;

    public static VariantOption create(String name, String value, ProductVariant variant) {
        VariantOption option = new VariantOption();
        option.name = Objects.requireNonNull(name, "Option name is required");
        option.value = Objects.requireNonNull(value, "Option value is required");
        option.variant = Objects.requireNonNull(variant, "Variant is required");
        return option;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariantOption that)) return false;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value) &&
                Objects.equals(variant, that.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}