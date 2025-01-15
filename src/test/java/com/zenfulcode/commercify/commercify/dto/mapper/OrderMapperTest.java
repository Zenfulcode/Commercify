package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
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

    private OrderEntity orderEntity;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        Set<OrderLineEntity> orderLines = new LinkedHashSet<>();
        OrderLineEntity orderLine = OrderLineEntity.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .unitPrice(99.99)
                .currency("USD")
                .build();
        orderLines.add(orderLine);

        orderEntity = OrderEntity.builder()
                .id(1L)
                .userId(1L)
                .orderLines(orderLines)
                .status(OrderStatus.PENDING)
                .currency("USD")
                .subTotal(199.98)
                .shippingCost(39.0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    @DisplayName("Should map OrderEntity to OrderDTO correctly")
    void apply_Success() {
        OrderDTO result = orderMapper.apply(orderEntity);

        assertNotNull(result);
        assertEquals(orderEntity.getId(), result.getId());
        assertEquals(orderEntity.getUserId(), result.getUserId());
        assertEquals(orderEntity.getStatus(), result.getOrderStatus());
        assertEquals(orderEntity.getCurrency(), result.getCurrency());
        assertEquals(orderEntity.getSubTotal(), result.getSubTotal());
        assertEquals(orderEntity.getShippingCost(), result.getShippingCost());
        assertEquals(orderEntity.getCreatedAt(), result.getCreatedAt());
        assertEquals(orderEntity.getUpdatedAt(), result.getUpdatedAt());
        assertEquals(orderEntity.getOrderLines().size(), result.getOrderLinesAmount());
    }

    @Test
    @DisplayName("Should handle OrderEntity with null values")
    void apply_HandlesNullValues() {
        OrderEntity emptyOrder = OrderEntity.builder()
                .id(1L)
                .build();

        OrderDTO result = orderMapper.apply(emptyOrder);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(0, result.getOrderLinesAmount());
        assertNull(result.getUserId());
        assertNull(result.getCurrency());
        assertEquals(0.0, result.getSubTotal());
        assertEquals(0.0, result.getShippingCost());
    }

    @Test
    @DisplayName("Should handle null totalAmount correctly")
    void apply_HandlesNullTotalAmount() {
        orderEntity.setSubTotal(null);

        OrderDTO result = orderMapper.apply(orderEntity);

        assertNotNull(result);
        assertEquals(0.0, result.getSubTotal());
    }

    @Test
    @DisplayName("Should handle null orderLines correctly")
    void apply_HandlesNullOrderLines() {
        orderEntity.setOrderLines(null);

        OrderDTO result = orderMapper.apply(orderEntity);

        assertNotNull(result);
        assertEquals(0, result.getOrderLinesAmount());
    }

    @Test
    @DisplayName("Should handle getTotalPrice correctly")
    void getTotalPrice() {
        OrderDTO result = orderMapper.apply(orderEntity);

        assertNotNull(result);
        assertEquals(238.98, result.getTotal());
    }
}