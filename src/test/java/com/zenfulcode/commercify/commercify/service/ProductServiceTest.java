package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.products.PriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.ProductRequest;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDeletionValidationResult;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.ProductDeletionException;
import com.zenfulcode.commercify.commercify.factory.ProductFactory;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.commercify.service.stripe.StripeProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private StripeProductService stripeProductService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductFactory productFactory;

    @InjectMocks
    private ProductService productService;

    private ProductRequest productRequest;
    private ProductEntity productEntity;
    private ProductDTO productDTO;
    private Set<OrderEntity> activeOrders;
    private List<OrderDTO> activeOrderDTOs;

    @BeforeEach
    void setUp() {
        PriceRequest priceRequest = new PriceRequest("USD", 99.99);
        productRequest = new ProductRequest(
                "Test Product",
                "Test Description",
                10,
                "test-image.jpg",
                true,
                priceRequest,
                new ArrayList<>()
        );

        productEntity = ProductEntity.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .stock(10)
                .active(true)
                .imageUrl("test-image.jpg")
                .currency("USD")
                .unitPrice(99.99)
                .stripeId("stripe_123")
                .stripePriceId("price_123")
                .build();

        productDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .stock(10)
                .active(true)
                .imageUrl("test-image.jpg")
                .currency("USD")
                .unitPrice(99.99)
                .build();

        activeOrders = new HashSet<>();
        activeOrderDTOs = new ArrayList<>();
    }

    // Existing test methods remain the same...
    // [Previous test methods remain unchanged]

    @Test
    @DisplayName("Should deactivate product successfully")
    void deactivateProduct_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
        doNothing().when(stripeProductService).deactivateProduct(any(ProductEntity.class));

        // Act
        productService.deactivateProduct(1L);

        // Assert
        assertFalse(productEntity.getActive());
        verify(productRepository).save(productEntity);
        verify(stripeProductService).deactivateProduct(productEntity);
    }

    @Test
    @DisplayName("Should throw exception when deactivating non-existent product")
    void deactivateProduct_ProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> productService.deactivateProduct(1L));
        verify(stripeProductService, never()).deactivateProduct(any());
    }

    @Test
    @DisplayName("Should reactivate product successfully")
    void reactivateProduct_Success() {
        // Arrange
        productEntity.setActive(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
        doNothing().when(stripeProductService).reactivateProduct(any(ProductEntity.class));

        // Act
        productService.reactivateProduct(1L);

        // Assert
        assertTrue(productEntity.getActive());
        verify(productRepository).save(productEntity);
        verify(stripeProductService).reactivateProduct(productEntity);
    }

    @Test
    @DisplayName("Should throw exception when reactivating non-existent product")
    void reactivateProduct_ProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> productService.reactivateProduct(1L));
        verify(stripeProductService, never()).reactivateProduct(any());
    }

    @Test
    @DisplayName("Should validate product deletion successfully")
    void validateProductDeletion_Success() {
        // Arrange
        when(orderLineRepository.findActiveOrdersForProduct(eq(1L), any())).thenReturn(new HashSet<>());

        // Act
        ProductDeletionValidationResult result = productService.validateProductDeletion(productEntity);

        // Assert
        assertTrue(result.canDelete());
        assertTrue(result.getIssues().isEmpty());
        assertTrue(result.getActiveOrders().isEmpty());
    }

    @Test
    @DisplayName("Should fail product deletion validation with active orders")
    void validateProductDeletion_WithActiveOrders() {
        // Arrange
        OrderEntity activeOrder = new OrderEntity();
        activeOrder.setId(1L);
        activeOrder.setStatus(OrderStatus.PENDING);
        activeOrders.add(activeOrder);

        OrderDTO orderDTO = OrderDTO.builder()
                .id(1L)
                .orderStatus(OrderStatus.PENDING)
                .build();

        when(orderLineRepository.findActiveOrdersForProduct(eq(1L), any())).thenReturn(activeOrders);
        when(orderMapper.apply(any(OrderEntity.class))).thenReturn(orderDTO);

        // Act
        ProductDeletionValidationResult result = productService.validateProductDeletion(productEntity);

        // Assert
        assertFalse(result.canDelete());
        assertEquals(1, result.getIssues().size());
        assertEquals(1, result.getActiveOrders().size());
        assertTrue(result.getIssues().get(0).contains("Product has 1 active orders"));
    }

    @Test
    @DisplayName("Should throw exception when deleting product with active orders")
    void deleteProduct_WithActiveOrders() {
        // Arrange
        OrderEntity activeOrder = new OrderEntity();
        activeOrder.setId(1L);
        activeOrder.setStatus(OrderStatus.PENDING);
        activeOrders.add(activeOrder);

        OrderDTO orderDTO = OrderDTO.builder()
                .id(1L)
                .orderStatus(OrderStatus.PENDING)
                .build();
        activeOrderDTOs.add(orderDTO);

        when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));
        when(orderLineRepository.findActiveOrdersForProduct(eq(1L), any())).thenReturn(activeOrders);
        when(orderMapper.apply(any(OrderEntity.class))).thenReturn(orderDTO);

        // Act & Assert
        ProductDeletionException exception = assertThrows(
                ProductDeletionException.class,
                () -> productService.deleteProduct(1L)
        );

        assertEquals("Cannot delete product", exception.getMessage());
        assertEquals(1, exception.getActiveOrders().size());
        verify(productRepository, never()).deleteById(any());
        verify(stripeProductService, never()).deactivateProduct(any());
    }

    @Test
    @DisplayName("Should handle Stripe API errors during product deletion")
    void deleteProduct_StripeError() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));
        when(orderLineRepository.findActiveOrdersForProduct(eq(1L), any())).thenReturn(new HashSet<>());
        doThrow(new RuntimeException("Stripe API error")).when(stripeProductService).deactivateProduct(any());

        // Act & Assert
        Exception exception = assertThrows(
                RuntimeException.class,
                () -> productService.deleteProduct(1L)
        );

        assertEquals("Stripe API error", exception.getMessage());
        verify(productRepository, never()).deleteById(any());
    }
}