package com.zenfulcode.commercify.order.domain.model;

import com.zenfulcode.commercify.order.domain.event.OrderCreatedEvent;
import com.zenfulcode.commercify.order.domain.event.OrderStatusChangedEvent;
import com.zenfulcode.commercify.order.domain.exception.InvalidOrderStateTransitionException;
import com.zenfulcode.commercify.order.domain.exception.OrderValidationException;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.shared.domain.model.AggregateRoot;
import com.zenfulcode.commercify.shared.domain.model.Money;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends AggregateRoot {
    @EmbeddedId
    private OrderId id;

    private UserId userId;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<OrderLine> orderLines = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_shipping_info_id")
    private OrderShippingInfo orderShippingInfo;

    @Column(name = "currency")
    private String currency;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "subtotal")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", insertable = false, updatable = false))
    })
    private Money subtotal;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "shipping_cost")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", insertable = false, updatable = false))
    })
    private Money shippingCost;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "tax")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", insertable = false, updatable = false))
    })
    private Money tax;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", insertable = false, updatable = false))
    })
    private Money totalAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Factory method
    public static Order create(
            UserId userId,
            String currency,
            OrderShippingInfo shippingInfo
    ) {
        Order order = new Order();
        order.id = OrderId.generate();
        order.userId = userId;
        order.currency = currency;
        order.status = OrderStatus.PENDING;
        order.orderShippingInfo = shippingInfo;
        order.totalAmount = Money.zero("USD");

        // Register domain event
        order.registerEvent(new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getCurrency()
        ));

        return order;
    }

    public void addOrderLine(OrderLine orderLine) {
        orderLines.add(orderLine);
        orderLine.setOrder(this);
        recalculateTotal();
    }

    public void removeOrderLine(OrderLine orderLine) {
        orderLines.remove(orderLine);
        orderLine.setOrder(null);
        recalculateTotal();
    }

    public void updateStatus(OrderStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new InvalidOrderStateTransitionException(
                    id,
                    status,
                    newStatus,
                    "Invalid order status transition"
            );
        }

        OrderStatus oldStatus = this.status;
        this.status = newStatus;

        registerEvent(new OrderStatusChangedEvent(
                this.id,
                oldStatus,
                newStatus
        ));
    }

    public void updateTotal() {
        this.totalAmount = subtotal
                .add(shippingCost != null ? shippingCost : Money.zero(currency))
                .add(tax != null ? tax : Money.zero(currency));
    }

    public void setSubtotal(Money subtotal) {
        validateSameCurrency(subtotal);
        this.subtotal = subtotal;
    }

    public void setShippingCost(Money shippingCost) {
        validateSameCurrency(shippingCost);
        this.shippingCost = shippingCost;
    }

    public void setTax(Money tax) {
        validateSameCurrency(tax);
        this.tax = tax;
    }

    private void validateSameCurrency(Money money) {
        if (!currency.equals(money.getCurrency())) {
            throw new OrderValidationException(
                    String.format("Currency mismatch: Expected %s but got %s",
                            currency, money.getCurrency())
            );
        }
    }

    private boolean canTransitionTo(OrderStatus newStatus) {
        return switch (status) {
            case PENDING -> Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED).contains(newStatus);
            case CONFIRMED -> Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED).contains(newStatus);
            case SHIPPED -> Set.of(OrderStatus.COMPLETED, OrderStatus.RETURNED).contains(newStatus);
            default -> false; // Terminal states
        };
    }

    private void recalculateTotal() {
        this.totalAmount = orderLines.stream()
                .map(OrderLine::getTotal)
                .reduce(Money.zero(currency), Money::add);
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }

    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED;
    }
}
