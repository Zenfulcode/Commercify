package com.zenfulcode.commercify.commercify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String name;
    private String description;
    private Integer stock;
    private String stripeId;
    private Boolean active;
    private String imageUrl;

    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default // This ensures the list is initialized when using builder
    private List<PriceEntity> prices = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addPrice(PriceEntity price) {
        prices.add(price);
        price.setProduct(this);
    }

    public void removePrice(PriceEntity price) {
        prices.remove(price);
        price.setProduct(null);
    }

    public void setPrices(List<PriceEntity> prices) {
        this.prices.clear();
        if (prices != null) {
            prices.forEach(this::addPrice);
        }
    }

    public PriceEntity getDefaultPrice() {
        return prices.stream()
                .filter(PriceEntity::getIsDefault)
                .findFirst()
                .orElse(null);
    }
}
