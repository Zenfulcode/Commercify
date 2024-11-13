package com.zenfulcode.commercify.commercify.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Integer stock;
    @Column(name = "stripe_id")
    private String stripeId;
    private Boolean active;
    @Column(name = "image_url")
    private String imageUrl;

    //    Prices
    private String currency;
    @Column(name = "unit_price")
    private Double unitPrice;
    @Column(name = "stripe_price_id")
    private String stripePriceId;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductVariantEntity> variants = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    public void addVariant(ProductVariantEntity variant) {
        variants.add(variant);
        variant.setProduct(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "name = " + name + ", " +
                "description = " + description + ", " +
                "stock = " + stock + ", " +
                "stripeId = " + stripeId + ", " +
                "active = " + active + ", " +
                "imageUrl = " + imageUrl + ", " +
                "currency = " + currency + ", " +
                "unitPrice = " + unitPrice + ", " +
                "stripePriceId = " + stripePriceId + ")";
    }
}
