package com.zenfulcode.commercify.commercify.service;


import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderLineRequest;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderLineMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private ProductService productService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderLineMapper orderLineMapper;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest orderRequest;
    private OrderEntity orderEntity;
    private OrderDTO orderDTO;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        CreateOrderLineRequest orderLineRequest = new CreateOrderLineRequest(1L, 2);
        orderRequest = new CreateOrderRequest(1L, "USD", List.of(orderLineRequest));

        orderEntity = OrderEntity.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .currency("USD")
                .totalAmount(100.0)
                .build();

        orderDTO = OrderDTO.builder()
                .id(1L)
                .userId(1L)
                .orderStatus(OrderStatus.PENDING)
                .currency("USD")
                .totalAmount(100.0)
                .build();

        productDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .stock(10)
                .unitPrice(50.0)
                .currency("USD")
                .build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void createOrder_Success() {
        when(productService.getProductById(anyLong())).thenReturn(productDTO);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        when(orderMapper.apply(any(OrderEntity.class))).thenReturn(orderDTO);

        OrderDTO result = orderService.createOrder(orderRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(OrderStatus.PENDING, result.getOrderStatus());
        assertEquals("USD", result.getCurrency());

        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderLineRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when creating order with invalid currency")
    void createOrder_InvalidCurrency() {
        CreateOrderRequest invalidRequest = new CreateOrderRequest(1L, "", List.of(new CreateOrderLineRequest(1L, 2)));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(invalidRequest));

        verify(orderRepository, never()).save(any(OrderEntity.class));
        verify(orderLineRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should update order status successfully")
    void updateOrderStatus_Success() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(orderEntity));

        orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent order status")
    void updateOrderStatus_NotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED));

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Should verify order ownership correctly")
    void isOrderOwnedByUser_Success() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(orderEntity));

        boolean result = orderService.isOrderOwnedByUser(1L, 1L);

        assertTrue(result);
        verify(orderRepository).findById(1L);
    }
}
