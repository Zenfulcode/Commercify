package com.zenfulcode.commercify.commercify.service;

import com.stripe.Stripe;
import com.zenfulcode.commercify.commercify.api.requests.products.CreateProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.CreatePriceRequest;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.exception.ProductValidationException;
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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

    private CreateProductRequest productRequest;
    private ProductEntity productEntity;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        CreatePriceRequest priceRequest = new CreatePriceRequest("USD", 99.99);
        productRequest = new CreateProductRequest(
                "Test Product",
                "Test Description",
                10,
                "test-image.jpg",
                true,
                priceRequest
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
    }

    @Test
    @DisplayName("Should save product successfully")
    void saveProduct_Success() {
        // Mock Stripe API key check
        try (MockedStatic<Stripe> stripeMock = mockStatic(Stripe.class)) {
            // Mock Stripe.apiKey to return a non-blank value
            Stripe.apiKey = "test-key";

            // Mock the stripe product creation to return an ID
            when(stripeProductService.createStripeProduct(any())).thenReturn("stripe_prod_123");

            when(productFactory.createFromRequest(any())).thenReturn(productEntity);
            when(productRepository.save(any(ProductEntity.class))).thenReturn(productEntity);
            when(productMapper.apply(any(ProductEntity.class))).thenReturn(productDTO);

            ProductDTO result = productService.saveProduct(productRequest);

            assertNotNull(result);
            assertEquals("Test Product", result.getName());
            assertEquals(99.99, result.getUnitPrice());

            verify(productRepository).save(any(ProductEntity.class));
            verify(stripeProductService).createStripeProduct(any(ProductEntity.class));
            verify(stripeProductService).createStripePrice(any(ProductEntity.class), any());
            Stripe.apiKey = "";
        }
    }

    @Test
    @DisplayName("Should throw exception when saving product with invalid data")
    void saveProduct_ValidationFailure() {
        CreateProductRequest invalidRequest = new CreateProductRequest(
                "",
                "Test Description", 10,
                "test-image.jpg",
                true,
                null
        );

        assertThrows(ProductValidationException.class, () -> productService.saveProduct(invalidRequest));

        verify(productRepository, never()).save(any(ProductEntity.class));
        verify(stripeProductService, never()).createStripeProduct(any(ProductEntity.class));
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void getProductById_Success() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(productEntity));
        when(productMapper.apply(any(ProductEntity.class))).thenReturn(productDTO);

        ProductDTO result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return null when product not found")
    void getProductById_NotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        ProductDTO result = productService.getProductById(1L);

        assertNull(result);
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get active products successfully")
    void getActiveProducts_Success() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ProductEntity> productPage = new PageImpl<>(List.of(productEntity));

        when(productRepository.queryAllByActiveTrue(any(PageRequest.class))).thenReturn(productPage);
        when(productMapper.apply(any(ProductEntity.class))).thenReturn(productDTO);

        Page<ProductDTO> result = productService.getActiveProducts(pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).queryAllByActiveTrue(pageRequest);
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProduct_Success() {

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(productEntity));

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productRepository).findById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void deleteProduct_NotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(1L));

        verify(productRepository).findById(1L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}