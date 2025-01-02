package com.zenfulcode.commercify.order.domain.model;

import com.zenfulcode.commercify.order.domain.valueobject.OrderLineId;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.model.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "order_lines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLine {
    @EmbeddedId
    private OrderLineId id;

    @Column(name = "product_id", nullable = false)
    private ProductId productId;

    @Column(nullable = false)
    private Integer quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Setter
    @ManyToOne(optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    // Factory method
    public static OrderLine create(
            ProductId productId,
            ProductVariant variant,
            Integer quantity,
            Money unitPrice
    ) {
        OrderLine line = new OrderLine();
        line.id = OrderLineId.generate();
        line.productId = productId;
        line.productVariant = variant;
        line.quantity = quantity;
        line.unitPrice = unitPrice;
        return line;
    }

    public Money getTotal() {
        return unitPrice.multiply(quantity);
    }

    // Equals and hashCode based on business identity
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderLine that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
