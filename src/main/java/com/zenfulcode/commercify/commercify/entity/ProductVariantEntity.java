package com.zenfulcode.commercify.commercify.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@Setter
@Entity
@Table(name = "variant_option_entity")
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private Integer stock;

    private String imageUrl;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String currency;

    @Column(name = "stripe_price_id")
    private String stripePriceId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<VariantOptionEntity> options = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    public void addOption(VariantOptionEntity option) {
        options.add(option);
        option.setProductVariant(this);
    }
}
