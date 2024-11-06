package com.zenfulcode.commercify.commercify.entity;


import com.zenfulcode.commercify.commercify.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderEntityTest {

    private OrderEntity order;

    @BeforeEach
    void setUp() {
        List<OrderLineEntity> orderLines = new ArrayList<>();
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
                .totalAmount(199.98)
                .createdAt(LocalDateTime.now())
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
        assertEquals(199.98, order.getTotalAmount());
    }

    @Test
    @DisplayName("Should manage order lines correctly")
    void testOrderLines() {
        assertEquals(1, order.getOrderLines().size());
        OrderLineEntity firstLine = order.getOrderLines().get(0);
        assertEquals(2, firstLine.getQuantity());
        assertEquals(99.99, firstLine.getUnitPrice());
        assertEquals(order, firstLine.getOrder());
    }

    @Test
    @DisplayName("Should handle empty order lines")
    void testEmptyOrderLines() {
        order.setOrderLines(new ArrayList<>());
        assertTrue(order.getOrderLines().isEmpty());
    }

    @Test
    @DisplayName("Should update status correctly")
    void testStatusUpdate() {
        order.setStatus(OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    @DisplayName("Should handle timestamps correctly")
    void testTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        assertEquals(now, order.getCreatedAt());
        assertEquals(now, order.getUpdatedAt());
    }
}