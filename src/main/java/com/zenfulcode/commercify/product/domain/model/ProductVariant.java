package com.zenfulcode.commercify.product.domain.model;

import com.zenfulcode.commercify.shared.domain.model.Money;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Builder
@Entity
@Table(name = "product_variants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    private Integer stock;

    private String imageUrl;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money price;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VariantOption> options = new HashSet<>();

    // Factory method
    public static ProductVariant create(String sku, Integer stock, Money price) {
        ProductVariant variant = new ProductVariant();
        variant.sku = Objects.requireNonNull(sku, "SKU is required");
        variant.stock = stock;
        variant.price = price;
        return variant;
    }

    // Domain methods
    public void updateStock(Integer stock) {
        if (stock != null && stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stock = stock;
    }

    public void addOption(String name, String value) {
        options.add(VariantOption.create(name, value, this));
    }

    public boolean hasActiveOrders() {
        // This would typically check a repository or domain service
        return false;
    }

    public Money getEffectivePrice() {
        return price != null ? price : product.getPrice();
    }

    public int getEffectiveStock() {
        return stock != null ? stock : product.getStock();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariant)) return false;
        ProductVariant that = (ProductVariant) o;
        return Objects.equals(sku, that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }

    public void updatePrice(Money newPrice) {
        throw new NotImplementedException("update price has not been implemented");
    }
}