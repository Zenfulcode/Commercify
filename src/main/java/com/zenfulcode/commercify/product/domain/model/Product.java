package com.zenfulcode.commercify.product.domain.model;

import com.zenfulcode.commercify.product.domain.event.ProductCreatedEvent;
import com.zenfulcode.commercify.product.domain.exception.InsufficientStockException;
import com.zenfulcode.commercify.product.domain.exception.ProductModificationException;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.model.AggregateRoot;
import com.zenfulcode.commercify.shared.domain.model.Money;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Builder
@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Product extends AggregateRoot {
    @EmbeddedId
    private ProductId id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private int stock;

    private String imageUrl;

    @Column(nullable = false)
    private boolean active;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money price;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "product")
    private Set<ProductVariant> variants = new HashSet<>();

    // Factory method for creating new products
    public static Product create(String name, String description, int stock, Money money) {
        Product product = new Product();
        product.id = ProductId.generate();
        product.name = Objects.requireNonNull(name, "Product name is required");
        product.description = description;
        product.stock = stock;
        product.price = Objects.requireNonNull(money, "Product price is required");
        product.active = true;

        // Register domain event
        product.registerEvent(new ProductCreatedEvent(
                product.getId(),
                product.getName(),
                product.getPrice()
        ));

        return product;
    }

    // Domain methods
    public void updateStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stock = quantity;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.stock += quantity;
    }

    public void removeStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > this.stock) {
            throw new InsufficientStockException(this.id, quantity, this.stock);
        }
        this.stock -= quantity;
    }

    public void updatePrice(Money newPrice) {
        this.price = Objects.requireNonNull(newPrice, "Price cannot be null");
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean hasEnoughStock(int quantity) {
        return this.stock >= quantity;
    }

    public void addVariant(ProductVariant variant) {
        Objects.requireNonNull(variant, "Variant cannot be null");
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        if (variant.hasActiveOrders()) {
            throw new ProductModificationException("Cannot remove variant with active orders");
        }
        variants.remove(variant);
        variant.setProduct(null);
    }

    public boolean hasVariant(String sku) {
        return variants.stream()
                .anyMatch(variant -> variant.getSku().equals(sku));
    }

    public Money getEffectivePrice(ProductVariant variant) {
        return variant != null && variant.getPrice() != null ?
                variant.getPrice() : this.price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Optional<ProductVariant> findVariantBySku(String sku) {
        throw new NotImplementedException("find variant by sku has not been implemented");
    }
}