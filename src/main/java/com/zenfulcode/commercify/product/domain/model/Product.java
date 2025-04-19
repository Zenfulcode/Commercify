package com.zenfulcode.commercify.product.domain.model;

import com.zenfulcode.commercify.product.domain.event.ProductCreatedEvent;
import com.zenfulcode.commercify.product.domain.event.ProductPriceUpdatedEvent;
import com.zenfulcode.commercify.product.domain.exception.InsufficientStockException;
import com.zenfulcode.commercify.product.domain.exception.ProductModificationException;
import com.zenfulcode.commercify.product.domain.valueobject.CategoryId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.model.AggregateRoot;
import com.zenfulcode.commercify.shared.domain.model.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends AggregateRoot {
    @EmbeddedId
    private ProductId id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "active", nullable = false)
    private Boolean active = false;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money price;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "category_id"))
    private CategoryId categoryId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "product")
    private Set<ProductVariant> productVariants = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public static Product create(String name, String description, String imageUrl, int stock, Money money) {
        Product product = new Product();
        product.id = ProductId.generate();
        product.name = Objects.requireNonNull(name, "Product name is required");
        product.description = description;
        product.imageUrl = imageUrl;
        product.stock = stock;
        product.price = Objects.requireNonNull(money, "Product price is required");
        product.active = true;

        // Register domain event
        product.registerEvent(new ProductCreatedEvent(
                product,
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

        registerEvent(new ProductPriceUpdatedEvent(
                this,
                id,
                newPrice
        ));
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
        productVariants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) {
        if (variant.hasActiveOrders()) {
            throw new ProductModificationException("Cannot remove variant with active orders");
        }
        productVariants.remove(variant);
        variant.setProduct(null);
    }

    public boolean hasVariant(String sku) {
        return productVariants.stream()
                .anyMatch(variant -> variant.getSku().equals(sku));
    }

    public Money getEffectivePrice(ProductVariant variant) {
        return variant != null && variant.getPrice() != null ?
                variant.getPrice() : this.price;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        this.name = name;
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

    public boolean isActive() {
        return active;
    }

    public boolean hasVariants() {
        return !productVariants.isEmpty();
    }
}