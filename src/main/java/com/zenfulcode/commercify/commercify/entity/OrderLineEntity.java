package com.zenfulcode.commercify.commercify.entity;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "OrderLines")
@NoArgsConstructor
public class OrderLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderline_id", nullable = false)
    private Long orderlineId;
    @Column(name = "stripe_product_id", updatable = false)
    private String stripeProductId;

    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;
    @Column(name = "quantity", nullable = false, updatable = false)
    private Integer quantity;
    @Column(name = "unit_price", nullable = false, updatable = false)
    private Double unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private OrderEntity order;

    @Transient
    private ProductDTO product;
}
