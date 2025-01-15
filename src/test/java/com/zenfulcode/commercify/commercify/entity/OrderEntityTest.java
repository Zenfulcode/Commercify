package com.zenfulcode.commercify.commercify.entity;


import com.zenfulcode.commercify.commercify.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderEntityTest {

    private OrderEntity order;

    @BeforeEach
    void setUp() {
        Set<OrderLineEntity> orderLines = new HashSet<>();
        OrderLineEntity orderLine = new OrderLineEntity();
        orderLine.setProductId(1L);
        orderLine.setQuantity(2);
        orderLine.setUnitPrice(99.99);
        orderLine.setCurrency("USD");
        orderLines.add(orderLine);

        order = OrderEntity.builder()
                .id(1L)
                .userId(1L)
                .orderLines(orderLines)
                .status(OrderStatus.PENDING)
                .currency("USD")
                .subTotal(199.98)
                .shippingCost(39.0)
                .build();

        // Set up bidirectional relationship
        orderLines.forEach(line -> line.setOrder(order));
    }

    @Test
    @DisplayName("Should create order with builder pattern")
    void testOrderBuilder() {
        assertNotNull(order);
        assertEquals(1L, order.getId());
        assertEquals(1L, order.getUserId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals("USD", order.getCurrency());
        assertEquals(199.98, order.getSubTotal());
        assertEquals(39.0, order.getShippingCost());
    }

    @Test
    @DisplayName("Should manage order lines correctly")
    void testOrderLines() {
        assertEquals(1, order.getOrderLines().size());
        OrderLineEntity firstLine = order.getOrderLines().stream().findFirst().orElse(null);
        assertEquals(2, firstLine.getQuantity());
        assertEquals(99.99, firstLine.getUnitPrice());
        assertEquals(order, firstLine.getOrder());
    }

    @Test
    @DisplayName("Should handle empty order lines")
    void testEmptyOrderLines() {
        order.setOrderLines(new HashSet<>());
        assertTrue(order.getOrderLines().isEmpty());
    }

    @Test
    @DisplayName("Should update status correctly")
    void testStatusUpdate() {
        order.setStatus(OrderStatus.PAID);
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    @DisplayName("Should handle timestamps correctly")
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(DateTimeException.class, () -> order.setCreatedAt(Instant.from(now)));
        assertThrows(DateTimeException.class, () -> order.setUpdatedAt(Instant.from(now)));
    }
}