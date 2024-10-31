package com.zenfulcode.commercify.commercify.entity;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "order_lines")
@NoArgsConstructor
public class OrderLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "price_id", nullable = false, updatable = false)
    private Long priceId;

    @Column(name = "stripe_price_id", updatable = false)
    private String stripePriceId;

    @Column(name = "quantity", nullable = false, updatable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, updatable = false)
    private Double unitPrice;

    @Column(name = "currency", nullable = false, updatable = false)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false, updatable = false)
    private OrderEntity order;

    @Transient
    private ProductDTO product;
}
