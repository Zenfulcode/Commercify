package com.zenfulcode.commercify.product.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "variant_options")
public class VariantOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    public static VariantOption create(String name, String value, ProductVariant variant) {
        VariantOption option = new VariantOption();
        option.name = Objects.requireNonNull(name, "Option name is required");
        option.value = Objects.requireNonNull(value, "Option value is required");
        option.productVariant = Objects.requireNonNull(variant, "Variant is required");
        return option;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariantOption that)) return false;
        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value) &&
                Objects.equals(productVariant, that.productVariant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}