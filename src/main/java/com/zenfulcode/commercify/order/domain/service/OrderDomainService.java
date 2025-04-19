package com.zenfulcode.commercify.order.domain.service;

import com.zenfulcode.commercify.order.application.query.FindAllOrdersQuery;
import com.zenfulcode.commercify.order.application.query.FindOrdersByUserIdQuery;
import com.zenfulcode.commercify.order.domain.exception.OrderNotFoundException;
import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderLine;
import com.zenfulcode.commercify.order.domain.model.OrderShippingInfo;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.repository.OrderRepository;
import com.zenfulcode.commercify.order.domain.valueobject.OrderDetails;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.order.domain.valueobject.OrderLineDetails;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.model.Money;
import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.service.UserDomainService;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDomainService {
    private final OrderPricingStrategy pricingStrategy;
    private final OrderValidationService validationService;

    private final OrderRepository orderRepository;

    private final UserDomainService userDomainService;

    public Order createOrder(OrderDetails orderDetails, List<Product> products, List<ProductVariant> variants) {
        // Create order with shipping info
        OrderShippingInfo shippingInfo = OrderShippingInfo.create(
                orderDetails.customerDetails(),
                orderDetails.shippingAddress(),
                orderDetails.billingAddress()
        );

        User customer = userDomainService.getUserById(orderDetails.customerId());

        Order order = Order.create(
                customer.getId(),
                orderDetails.currency(),
                shippingInfo
        );

        order.setUser(customer);

        // Map products and variants for lookup
        Map<ProductId, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Map<ProductId, ProductVariant> variantMap = variants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        // Add order lines with validation
        for (OrderLineDetails lineDetails : orderDetails.orderLines()) {
            Product product = productMap.get(lineDetails.productId());
            // Using validationService for stock validation
            validationService.validateStock(product, lineDetails.quantity());

            ProductVariant variant = null;
            if (lineDetails.variantId() != null) {
                variant = variantMap.get(lineDetails.variantId());
                validationService.validateVariant(variant, product, lineDetails);
            }

            OrderLine line = OrderLine.create(
                    product,
                    variant,
                    lineDetails.quantity()
            );

            line.setProduct(product);

            order.addOrderLine(line);
        }

        // Apply pricing
        applyPricing(order);

        // Using validationService for order validation
        validationService.validateCreateOrder(order);

        orderRepository.save(order);

        return order;
    }

    private void applyPricing(Order order) {
        Money subtotal = pricingStrategy.calculateSubtotal(order);
        order.setSubtotal(subtotal);

//        Money shippingCost = pricingStrategy.calculateShippingCost(order);
//        order.setShippingCost(shippingCost);

        Money tax = pricingStrategy.calculateTax(order);
        order.setTax(tax);

        order.updateTotal();
        orderRepository.save(order);
    }

    public void updateOrderStatus(Order order, OrderStatus newStatus) {
        // Using validationService for status transition validation
        validationService.validateStatusTransition(order, newStatus);

        order.updateStatus(newStatus);
        orderRepository.save(order);
    }

    public Order getOrderById(OrderId orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public Page<Order> findOrdersByUserId(FindOrdersByUserIdQuery query) {
        return orderRepository.findByUserId(query.userId(), query.pageRequest());
    }

    public Page<Order> findAllOrders(FindAllOrdersQuery query) {
        return orderRepository.findAll(query.pageRequest());
    }

    public boolean isOrderOwnedByUser(OrderId orderId, UserId userId) {
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }

    public BigDecimal calculateTotalRevenue(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay().toInstant(ZoneOffset.from(ZoneOffset.UTC));
        Instant end = endDate.atTime(23, 59).toInstant(ZoneOffset.UTC);

        // Implement logic to calculate total revenue based on the provided parameters
        // This is a placeholder implementation and should be replaced with actual logic
        return orderRepository.calculateTotalRevenue(start, end)
                .orElse(BigDecimal.ZERO);
    }

    public int countOrdersInPeriod(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay().toInstant(ZoneOffset.from(ZoneOffset.UTC));
        Instant end = endDate.atTime(23, 59).toInstant(ZoneOffset.UTC);

        return orderRepository.countOrders(start, end);
    }
}
