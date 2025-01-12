package com.zenfulcode.commercify.order.domain.model;

import com.zenfulcode.commercify.order.domain.valueobject.OrderLineId;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.shared.domain.model.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_lines")
public class OrderLine {
    @EmbeddedId
    private OrderLineId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", insertable = false, updatable = false))
    })
    private Money unitPrice;

    public static OrderLine create(
            Product product,
            ProductVariant variant,
            Integer quantity
    ) {
        OrderLine line = new OrderLine();
        line.id = OrderLineId.generate();
        line.product = product;
        line.productVariant = variant;
        line.quantity = quantity;
        line.unitPrice = product.getEffectivePrice(variant);
        return line;
    }

    public Money getTotal() {
        return unitPrice.multiply(quantity);
    }
}