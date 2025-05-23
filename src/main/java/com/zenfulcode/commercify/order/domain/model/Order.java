package com.zenfulcode.commercify.order.domain.model;

import com.zenfulcode.commercify.order.domain.event.OrderCreatedEvent;
import com.zenfulcode.commercify.order.domain.event.OrderStatusChangedEvent;
import com.zenfulcode.commercify.order.domain.exception.OrderValidationException;
import com.zenfulcode.commercify.order.domain.service.OrderStateFlow;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.shared.domain.model.AggregateRoot;
import com.zenfulcode.commercify.shared.domain.model.Money;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends AggregateRoot {
    @EmbeddedId
    private OrderId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "currency")
    private String currency;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderLine> orderLines = new LinkedHashSet<>();

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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "order_shipping_info_id")
    private OrderShippingInfo orderShippingInfo;

    @CreationTimestamp
    @Column(name = "created_at")
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
        order.currency = currency;
        order.status = OrderStatus.PENDING;
        order.orderShippingInfo = shippingInfo;
        order.totalAmount = Money.zero("USD");

        // Register domain event
        order.registerEvent(new OrderCreatedEvent(
                order,
                order.getId(),
                userId,
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
        OrderStatus oldStatus = this.status;
        this.status = newStatus;

        registerEvent(new OrderStatusChangedEvent(
                this,
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

    private void recalculateTotal() {
        this.totalAmount = orderLines.stream()
                .map(OrderLine::getTotal)
                .reduce(Money.zero(currency), Money::add);
    }

    public boolean isInTerminalState(OrderStateFlow stateFlow) {
        return stateFlow.isTerminalState(status);
    }

    public Set<OrderStatus> getValidTransitions(OrderStateFlow stateFlow) {
        return stateFlow.getValidTransitions(status);
    }

    public boolean isCompleted() {
        return status == OrderStatus.COMPLETED;
    }
}
