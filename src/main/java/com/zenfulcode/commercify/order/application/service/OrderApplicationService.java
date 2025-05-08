package com.zenfulcode.commercify.order.application.service;

import com.zenfulcode.commercify.order.application.command.CancelOrderCommand;
import com.zenfulcode.commercify.order.application.command.CreateOrderCommand;
import com.zenfulcode.commercify.order.application.command.GetOrderByIdCommand;
import com.zenfulcode.commercify.order.application.command.UpdateOrderStatusCommand;
import com.zenfulcode.commercify.order.application.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.order.application.query.CalculateTotalRevenueQuery;
import com.zenfulcode.commercify.order.application.query.CountOrdersInPeriodQuery;
import com.zenfulcode.commercify.order.application.query.FindAllOrdersQuery;
import com.zenfulcode.commercify.order.application.query.FindOrdersByUserIdQuery;
import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.service.OrderDomainService;
import com.zenfulcode.commercify.order.domain.valueobject.OrderDetails;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.order.domain.valueobject.OrderLineDetails;
import com.zenfulcode.commercify.product.application.service.ProductApplicationService;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantId;
import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {
    private final OrderDomainService orderDomainService;
    private final DomainEventPublisher eventPublisher;
    private final ProductApplicationService productApplicationService;

    @Transactional
    public OrderId createOrder(CreateOrderCommand command) {
        // Get products and variants
        List<ProductId> productIds = command.orderLines()
                .stream()
                .map(OrderLineDetails::productId)
                .collect(Collectors.toList());

        List<Product> products = productApplicationService.findAllProducts(productIds);

        List<VariantId> variantIds = command.orderLines()
                .stream()
                .map(OrderLineDetails::variantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<ProductVariant> variants = productApplicationService.findVariantsByIds(variantIds);

        // Create order through domain service
        Order order = orderDomainService.createOrder(
                OrderDetails.builder()
                        .customerId(command.customerId())
                        .currency(command.currency())
                        .customerDetails(command.customerDetails())
                        .shippingAddress(command.shippingAddress())
                        .billingAddress(command.billingAddress())
                        .orderLines(command.orderLines())
                        .build(),
                products,
                variants
        );

        // publish events
        eventPublisher.publish(order.getDomainEvents());

        return order.getId();
    }

    @Transactional
    public void updateOrderStatus(UpdateOrderStatusCommand command) {
        Order order = orderDomainService.getOrderById(command.orderId());
        orderDomainService.updateOrderStatus(order, command.newStatus());

        // Save and publish events
        eventPublisher.publish(order.getDomainEvents());
    }

    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
        Order order = orderDomainService.getOrderById(command.orderId());
        orderDomainService.updateOrderStatus(order, OrderStatus.CANCELLED);
        eventPublisher.publish(order.getDomainEvents());
    }

    @Transactional(readOnly = true)
    public Page<Order> findOrdersByUserId(FindOrdersByUserIdQuery query) {
        return orderDomainService.findOrdersByUserId(query);
    }

    @Transactional(readOnly = true)
    public Page<Order> findAllOrders(FindAllOrdersQuery query) {
        return orderDomainService.findAllOrders(query);
    }

    @Transactional(readOnly = true)
    public OrderDetailsDTO getOrderDetailsById(GetOrderByIdCommand command) {
        OrderId orderId = OrderId.of(command.orderId());
        Order order = getOrderById(orderId);
        return OrderDetailsDTO.fromOrder(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(OrderId orderId) {
        return orderDomainService.getOrderById(orderId);
    }

    @Transactional(readOnly = true)
    public boolean isOrderOwnedByUser(OrderId orderId, UserId userId) {
        return orderDomainService.isOrderOwnedByUser(orderId, userId);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue(CalculateTotalRevenueQuery query) {
        return orderDomainService.calculateTotalRevenue(query.startDate(), query.endDate());
    }

    public int countOrdersInPeriod(CountOrdersInPeriodQuery query) {
        return orderDomainService.countOrdersInPeriod(query.startDate(), query.endDate());
    }

    public void canCaptureOrder(OrderId orderId) {
        orderDomainService.canCaptureOrder(orderId);
    }
}