package com.zenfulcode.commercify.dto.mapper;

import com.zenfulcode.commercify.domain.enums.OrderStatus;
import com.zenfulcode.commercify.web.dto.common.OrderDTO;
import com.zenfulcode.commercify.domain.model.Order;
import com.zenfulcode.commercify.domain.model.OrderLine;
import com.zenfulcode.commercify.web.dto.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    @InjectMocks
    private OrderMapper orderMapper;

    private Order order;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        Set<OrderLine> orderLines = new LinkedHashSet<>();
        OrderLine orderLine = OrderLine.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .unitPrice(99.99)
                .currency("USD")
                .build();
        orderLines.add(orderLine);

        order = Order.builder()
                .id(1L)
                .userId(1L)
                .orderLines(orderLines)
                .status(OrderStatus.PENDING)
                .currency("USD")
                .totalAmount(199.98)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    @DisplayName("Should map OrderEntity to OrderDTO correctly")
    void apply_Success() {
        OrderDTO result = orderMapper.apply(order);

        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
        assertEquals(order.getUserId(), result.getUserId());
        assertEquals(order.getStatus(), result.getOrderStatus());
        assertEquals(order.getCurrency(), result.getCurrency());
        assertEquals(order.getTotalAmount(), result.getTotalAmount());
        assertEquals(order.getCreatedAt(), result.getCreatedAt());
        assertEquals(order.getUpdatedAt(), result.getUpdatedAt());
        assertEquals(order.getOrderLines().size(), result.getOrderLinesAmount());
    }

    @Test
    @DisplayName("Should handle OrderEntity with null values")
    void apply_HandlesNullValues() {
        Order emptyOrder = Order.builder()
                .id(1L)
                .build();

        OrderDTO result = orderMapper.apply(emptyOrder);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(0, result.getOrderLinesAmount());
        assertNull(result.getUserId());
        assertNull(result.getCurrency());
        assertEquals(0.0, result.getTotalAmount());
    }

    @Test
    @DisplayName("Should handle null totalAmount correctly")
    void apply_HandlesNullTotalAmount() {
        order.setTotalAmount(null);

        OrderDTO result = orderMapper.apply(order);

        assertNotNull(result);
        assertEquals(0.0, result.getTotalAmount());
    }

    @Test
    @DisplayName("Should handle null orderLines correctly")
    void apply_HandlesNullOrderLines() {
        order.setOrderLines(null);

        OrderDTO result = orderMapper.apply(order);

        assertNotNull(result);
        assertEquals(0, result.getOrderLinesAmount());
    }
}