package com.zenfulcode.commercify.order.domain.service;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderLine;
import com.zenfulcode.commercify.order.domain.model.OrderShippingInfo;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.valueobject.OrderDetails;
import com.zenfulcode.commercify.order.domain.valueobject.OrderLineDetails;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.model.Money;
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

    public Order createOrder(OrderDetails orderDetails, List<Product> products, List<ProductVariant> variants) {
        // Create order with shipping info
        OrderShippingInfo shippingInfo = OrderShippingInfo.create(
                orderDetails.customerDetails(),
                orderDetails.shippingAddress(),
                orderDetails.billingAddress()
        );

        Order order = Order.create(
                orderDetails.customerId(),
                orderDetails.currency(),
                shippingInfo
        );

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

            Money linePrice = calculateLinePrice(product, variant, lineDetails.quantity());
            OrderLine line = OrderLine.create(
                    product.getId(),
                    variant,
                    lineDetails.quantity(),
                    linePrice
            );

            order.addOrderLine(line);
        }

        // Apply pricing
        applyPricing(order);

        // Using validationService for order validation
        validationService.validateCreateOrder(order);

        return order;
    }

    private void applyPricing(Order order) {
        Money subtotal = pricingStrategy.calculateSubtotal(order);
        order.setSubtotal(subtotal);

        Money shippingCost = pricingStrategy.calculateShippingCost(order);
        order.setShippingCost(shippingCost);

        Money tax = pricingStrategy.calculateTax(order);
        order.setTax(tax);

        order.updateTotal();
    }

    public void updateOrderStatus(Order order, OrderStatus newStatus) {
        // Using validationService for status transition validation
        validationService.validateStatusTransition(order, newStatus);

        if (newStatus == OrderStatus.CANCELLED) {
            validationService.validateOrderCancellation(order);
        } else if (newStatus == OrderStatus.COMPLETED) {
            validationService.validateOrderCompletion(order);
        }

        order.updateStatus(newStatus);
    }

    private Money calculateLinePrice(Product product, ProductVariant variant, int quantity) {
        Money unitPrice = variant != null ?
                variant.getEffectivePrice() :
                product.getPrice();
        return unitPrice.multiply(quantity);
    }
}
