package com.zenfulcode.commercify.commercify.entity;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
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

    @Column(name = "quantity", nullable = false, updatable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, updatable = false)
    private Double unitPrice;

    @Column(name = "currency", nullable = false, updatable = false)
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    @ToString.Exclude
    private OrderEntity order;

    @Transient
    private ProductDTO product;
}
