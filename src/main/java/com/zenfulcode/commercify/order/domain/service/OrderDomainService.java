package com.zenfulcode.commercify.order.domain.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
