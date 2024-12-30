package com.zenfulcode.commercify.service.order;

import com.zenfulcode.commercify.domain.enums.OrderStatus;
import com.zenfulcode.commercify.domain.model.Order;
import com.zenfulcode.commercify.domain.model.OrderLine;
import com.zenfulcode.commercify.domain.model.Product;
import com.zenfulcode.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.repository.OrderShippingInfoRepository;
import com.zenfulcode.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.service.StockManagementService;
import com.zenfulcode.commercify.service.core.OrderService;
import com.zenfulcode.commercify.service.validations.OrderValidationService;
import com.zenfulcode.commercify.web.dto.common.AddressDTO;
import com.zenfulcode.commercify.web.dto.common.CustomerDetailsDTO;
import com.zenfulcode.commercify.web.dto.common.OrderDTO;
import com.zenfulcode.commercify.web.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.web.dto.request.order.CreateOrderLineRequest;
import com.zenfulcode.commercify.web.dto.request.order.CreateOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderValidationService validationService;
    @Mock
    private StockManagementService stockService;
    @Mock
    private OrderShippingInfoRepository orderShippingInfoRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderDTO orderDTO;
    private CreateOrderRequest createOrderRequest;
    private Product productEntity;

    @BeforeEach
    void setUp() {
        productEntity = com.zenfulcode.commercify.domain.model.Product.builder()
                .id(1L)
                .name("Test Product")
                .active(true)
                .stock(10)
                .unitPrice(99.99)
                .currency("USD")
                .build();

        OrderLine orderLine = OrderLine.builder()
                .id(1L)
                .productId(1L)
                .quantity(2)
                .unitPrice(99.99)
                .currency("USD")
                .build();

        CustomerDetailsDTO customerDetailsDTO = CustomerDetailsDTO.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@email.com")
                .phone("1234567890")
                .build();

        order = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .currency("USD")
                .totalAmount(199.98)
                .orderLines(Set.of(orderLine))
                .createdAt(Instant.now())
                .build();

        orderDTO = OrderDTO.builder()
                .id(1L)
                .userId(1L)
                .orderStatus(OrderStatus.PENDING)
                .currency("USD")
                .totalAmount(199.98)
                .build();

        AddressDTO addressDTO = AddressDTO.builder()
                .street("Test Street")
                .city("Test City")
                .state("Test State")
                .zipCode("12345")
                .country("Test Country")
                .build();

        CreateOrderLineRequest orderLineRequest = new CreateOrderLineRequest(1L, null, 2);
        createOrderRequest = new CreateOrderRequest("USD", customerDetailsDTO, List.of(orderLineRequest), addressDTO, null);
    }

    @Nested
    @DisplayName("Create Order Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create order successfully")
        void createOrder_Success() {
            when(productRepository.findAllById(any())).thenReturn(List.of(productEntity));
            when(orderRepository.save(any())).thenReturn(order);
            when(orderMapper.apply(any())).thenReturn(orderDTO);

            OrderDTO result = orderService.createOrder(1L, createOrderRequest);

            assertNotNull(result);
            assertEquals(orderDTO.getId(), result.getId());
            assertEquals(orderDTO.getTotalAmount(), result.getTotalAmount());
            verify(validationService).validateCreateOrderRequest(createOrderRequest);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void createOrder_ProductNotFound() {
            when(productRepository.findAllById(any())).thenReturn(Collections.emptyList());

            assertThrows(ProductNotFoundException.class,
                    () -> orderService.createOrder(1L, createOrderRequest));
        }
    }

    @Nested
    @DisplayName("Update Order Status Tests")
    class UpdateOrderStatusTests {

        @Test
        @DisplayName("Should update order status successfully")
        void updateOrderStatus_Success() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any())).thenReturn(order);

            assertDoesNotThrow(() ->
                    orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED));
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        }

        @Test
        @DisplayName("Should throw exception when order not found")
        void updateOrderStatus_OrderNotFound() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(OrderNotFoundException.class,
                    () -> orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED));
        }
    }

    @Nested
    @DisplayName("Get Orders Tests")
    class GetOrdersTests {

        @Test
        @DisplayName("Should get orders by user ID")
        void getOrdersByUserId_Success() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<Order> orderPage = new PageImpl<>(List.of(order));

            when(orderRepository.findByUserId(1L, pageRequest)).thenReturn(orderPage);
            when(orderMapper.apply(any())).thenReturn(orderDTO);

            Page<OrderDTO> result = orderService.getOrdersByUserId(1L, pageRequest);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(orderRepository).findByUserId(1L, pageRequest);
        }

        @Test
        @DisplayName("Should get all orders")
        void getAllOrders_Success() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<Order> orderPage = new PageImpl<>(List.of(order));

            when(orderRepository.findAll(pageRequest)).thenReturn(orderPage);
            when(orderMapper.apply(any())).thenReturn(orderDTO);

            Page<OrderDTO> result = orderService.getAllOrders(pageRequest);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
        }
    }

    @Nested
    @DisplayName("Cancel Order Tests")
    class CancelOrderTests {

        @Test
        @DisplayName("Should cancel order successfully")
        void cancelOrder_Success() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            doNothing().when(validationService).validateOrderCancellation(order);
            when(orderRepository.save(any())).thenReturn(order);

            assertDoesNotThrow(() -> orderService.cancelOrder(1L));
            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            verify(stockService).restoreStockLevels(order.getOrderLines());
        }

        @Test
        @DisplayName("Should throw exception when cancelling non-existent order")
        void cancelOrder_OrderNotFound() {
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(OrderNotFoundException.class,
                    () -> orderService.cancelOrder(1L));
            verify(stockService, never()).restoreStockLevels(any());
        }
    }

    @Nested
    @DisplayName("Order Ownership Tests")
    class OrderOwnershipTests {

        @Test
        @DisplayName("Should verify order ownership")
        void isOrderOwnedByUser_Success() {
            when(orderRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

            assertTrue(orderService.isOrderOwnedByUser(1L, 1L));
        }

        @Test
        @DisplayName("Should verify order not owned by user")
        void isOrderOwnedByUser_NotOwned() {
            when(orderRepository.existsByIdAndUserId(1L, 2L)).thenReturn(false);

            assertFalse(orderService.isOrderOwnedByUser(1L, 2L));
        }
    }
}