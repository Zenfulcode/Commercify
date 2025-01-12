package com.zenfulcode.commercify.product.domain.model;

import com.zenfulcode.commercify.product.domain.valueobject.VariantId;
import com.zenfulcode.commercify.shared.domain.model.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "product_variants", uniqueConstraints = {
        @UniqueConstraint(name = "uc_product_variants_sku", columnNames = {"sku"})
})
public class ProductVariant {
    @EmbeddedId
    private VariantId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false)
    private String sku;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "image_url")
    private String imageUrl;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money price;

    @OneToMany(mappedBy = "productVariant")
    private Set<VariantOption> variantOptions = new LinkedHashSet<>();

    public static ProductVariant create(String sku, Integer stock, Money price, String imageUrl) {
        ProductVariant variant = new ProductVariant();
        variant.id = VariantId.generate();
        variant.sku = Objects.requireNonNull(sku, "SKU is required");
        variant.stock = stock;
        variant.price = price;
        variant.imageUrl = imageUrl;
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
        variantOptions.add(VariantOption.create(name, value, this));
    }

    public boolean hasActiveOrders() {
        // TODO: This would typically check a repository or domain service
        return false;
    }

    public boolean belongsTo(Product product) {
        return this.product.getId().equals(product.getId());
    }

    public boolean hasEnoughStock(int requestedQuantity) {
        if (stock == null) {
            // If variant doesn't manage its own stock, delegate to product
            return product.hasEnoughStock(requestedQuantity);
        }
        return stock >= requestedQuantity;
    }

    public int getEffectiveStock() {
        return stock != null ? stock : product.getStock();
    }

    public Money getEffectivePrice() {
        return price != null ? price : product.getPrice();
    }

    public void updatePrice(Money newPrice) {
        this.price = Objects.requireNonNull(newPrice, "Price cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariant that)) return false;
        return Objects.equals(sku, that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }
}