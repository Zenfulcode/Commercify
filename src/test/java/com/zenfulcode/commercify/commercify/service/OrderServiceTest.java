package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderLineRequest;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.PriceDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @InjectMocks
    private OrderService orderService;

    @Nested
    @DisplayName("Order Creation Tests")
    class OrderCreationTests {

        @Test
        @DisplayName("Should successfully create an order")
        void createOrder_Success() {
            // Arrange
            PriceDTO priceDTO = PriceDTO.builder()
                    .id(1L)
                    .currency("USD")
                    .amount(99.99)
                    .isDefault(true)
                    .active(true)
                    .build();

            ProductDTO productDTO = ProductDTO.builder()
                    .id(1L)
                    .name("Test Product")
                    .description("Test Description")
                    .stock(10)
                    .active(true)
                    .prices(List.of(priceDTO))
                    .build();

            OrderEntity savedOrder = OrderEntity.builder()
                    .id(1L)
                    .userId(1L)
                    .status(OrderStatus.PENDING)
                    .currency("USD")
                    .totalAmount(199.98)
                    .build();

            OrderDTO expectedOrderDTO = OrderDTO.builder()
                    .id(1L)
                    .userId(1L)
                    .orderStatus(OrderStatus.PENDING)
                    .currency("USD")
                    .totalAmount(199.98)
                    .build();

            CreateOrderRequest request = new CreateOrderRequest(
                    1L,
                    "USD",
                    List.of(new CreateOrderLineRequest(1L, 2))
            );

            // Mock behaviors
            when(productService.getProductById(1L)).thenReturn(productDTO);
            when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
            when(orderMapper.apply(any(OrderEntity.class))).thenReturn(expectedOrderDTO);

            // Act
            OrderDTO result = orderService.createOrder(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getCurrency()).isEqualTo("USD");
            assertThat(result.getTotalAmount()).isEqualTo(199.98);

            // Verify interactions
            verify(productService).getProductById(1L);
            verify(orderRepository).save(any(OrderEntity.class));
            verify(orderLineRepository).saveAll(any());
            verify(orderMapper).apply(any(OrderEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void createOrder_ProductNotFound() {
            // Arrange
            CreateOrderRequest request = new CreateOrderRequest(
                    1L,
                    "USD",
                    List.of(new CreateOrderLineRequest(1L, 2))
            );

            when(productService.getProductById(1L)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Product not found");

            // Verify
            verify(orderRepository, never()).save(any());
            verify(orderLineRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("Should throw exception for insufficient stock")
        void createOrder_InsufficientStock() {
            // Arrange
            PriceDTO priceDTO = PriceDTO.builder()
                    .id(1L)
                    .currency("USD")
                    .amount(99.99)
                    .isDefault(true)
                    .active(true)
                    .build();

            ProductDTO productDTO = ProductDTO.builder()
                    .id(1L)
                    .name("Test Product")
                    .description("Test Description")
                    .stock(1) // Only 1 in stock
                    .active(true)
                    .prices(List.of(priceDTO))
                    .build();

            CreateOrderRequest request = new CreateOrderRequest(
                    1L,
                    "USD",
                    List.of(new CreateOrderLineRequest(1L, 2)) // Trying to order 2
            );

            when(productService.getProductById(1L)).thenReturn(productDTO);

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Insufficient stock");

            // Verify
            verify(orderRepository, never()).save(any());
            verify(orderLineRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("Should throw exception for invalid request")
        void createOrder_InvalidRequest() {
            // Arrange
            CreateOrderRequest invalidRequest = new CreateOrderRequest(
                    null, // Invalid userId
                    "USD",
                    List.of(new CreateOrderLineRequest(1L, 2))
            );

            // Act & Assert
            assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid order request");

            // Verify
            verify(productService, never()).getProductById(any());
            verify(orderRepository, never()).save(any());
            verify(orderLineRepository, never()).saveAll(any());
        }
    }
}