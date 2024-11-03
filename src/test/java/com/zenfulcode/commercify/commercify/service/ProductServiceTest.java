package com.zenfulcode.commercify.commercify.service;

import com.stripe.Stripe;
import com.zenfulcode.commercify.commercify.api.requests.products.CreatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.CreateProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdateProductRequest;
import com.zenfulcode.commercify.commercify.config.StripeConfig;
import com.zenfulcode.commercify.commercify.dto.PriceDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.ProductUpdateResult;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.commercify.entity.PriceEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.exception.ProductValidationException;
import com.zenfulcode.commercify.commercify.factory.ProductFactory;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.commercify.service.stripe.StripeProductService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private StripeConfig stripeConfig;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PriceService priceService;

    @Mock
    private StripeProductService stripeProductService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductFactory productFactory;

    @Mock
    private OrderLineRepository orderLineRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private ProductService productService;

    private ProductEntity mockProductEntity;
    private ProductDTO mockProductDTO;
    private CreateProductRequest validCreateRequest;
    private UpdateProductRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        mockProductEntity = ProductEntity.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .stock(10)
                .active(true)
                .price(null)
                .build();

        mockProductDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .stock(10)
                .active(true)
                .price(null)
                .build();

        PriceDTO mockPriceDTO = PriceDTO.builder()
                .currency("USD")
                .amount(99.99)
                .active(true)
                .build();

        mockProductDTO.setPrice(mockPriceDTO);

        validCreateRequest = new CreateProductRequest(
                "Test Product",
                "Test Description",
                10,
                "image.jpg",
                true,
                new CreatePriceRequest("USD", 99.99, true)
        );

        validUpdateRequest = new UpdateProductRequest(
                "Updated Product",
                "Updated Description",
                15,
                "updated-image.jpg",
                true,
                null
        );
    }

    @Nested
    @DisplayName("Product Creation Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should successfully create a product")
        void shouldCreateProduct() {
            // Arrange
            PriceEntity mockPrice = PriceEntity.builder()
                    .id(1L)
                    .currency("USD")
                    .amount(99.99)
                    .active(true)
                    .build();

            when(productFactory.createFromRequest(any())).thenReturn(mockProductEntity);
            when(priceService.createPrice(any(), any())).thenReturn(mockPrice);
            when(productRepository.save(any())).thenReturn(mockProductEntity);
            when(productMapper.apply(any())).thenReturn(mockProductDTO);

            // Act
            ProductDTO result = productService.saveProduct(validCreateRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(mockProductDTO.getName());
            verify(productRepository).save(any(ProductEntity.class));
            verify(productMapper).apply(any(ProductEntity.class));
            verify(priceService).createPrice(any(), any());
        }

        @Test
        @DisplayName("Should create product with Stripe integration")
        void shouldCreateProductWithStripe() {
            // Arrange
            PriceEntity mockPrice = PriceEntity.builder()
                    .id(1L)
                    .currency("USD")
                    .amount(99.99)
                    .active(true)
                    .build();

            Stripe.apiKey = "test_key";
            when(productFactory.createFromRequest(any())).thenReturn(mockProductEntity);
            when(priceService.createPrice(any(), any())).thenReturn(mockPrice);
            when(productRepository.save(any())).thenReturn(mockProductEntity);
            when(productMapper.apply(any())).thenReturn(mockProductDTO);
            when(stripeProductService.createStripeProduct(any())).thenReturn("stripe_id");

            // Act
            ProductDTO result = productService.saveProduct(validCreateRequest);

            // Assert
            assertThat(result).isNotNull();
            verify(stripeProductService).createStripeProduct(any());
            verify(priceService).createPrice(any(), any());

            // Cleanup
            Stripe.apiKey = null;
        }

        @Test
        @DisplayName("Should throw exception when product name is null")
        void shouldThrowExceptionForNullName() {
            // Arrange
            CreateProductRequest invalidRequest = new CreateProductRequest(
                    null,
                    "Test Description",
                    10,
                    "image.jpg",
                    true,
                    new CreatePriceRequest("USD", 99.99, true)
            );

            // Act & Assert
            assertThatThrownBy(() -> productService.saveProduct(invalidRequest))
                    .isInstanceOf(ProductValidationException.class)
                    .hasMessageContaining("Product name is required");

            verify(productRepository, never()).save(any());
            verify(priceService, never()).createPrice(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when product name is empty")
        void shouldThrowExceptionForEmptyName() {
            // Arrange
            CreateProductRequest invalidRequest = new CreateProductRequest(
                    "",
                    "Test Description",
                    10,
                    "image.jpg",
                    true,
                    new CreatePriceRequest("USD", 99.99, true)
            );

            // Act & Assert
            assertThatThrownBy(() -> productService.saveProduct(invalidRequest))
                    .isInstanceOf(ProductValidationException.class)
                    .hasMessageContaining("Product name is required");

            verify(productRepository, never()).save(any());
            verify(priceService, never()).createPrice(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when product name is blank")
        void shouldThrowExceptionForBlankName() {
            // Arrange
            CreateProductRequest invalidRequest = new CreateProductRequest(
                    "   ",
                    "Test Description",
                    10,
                    "image.jpg",
                    true,
                    new CreatePriceRequest("USD", 99.99, true)
            );

            // Act & Assert
            assertThatThrownBy(() -> productService.saveProduct(invalidRequest))
                    .isInstanceOf(ProductValidationException.class)
                    .hasMessageContaining("Product name is required");

            verify(productRepository, never()).save(any());
            verify(priceService, never()).createPrice(any(), any());
        }
    }

    @Nested
    @DisplayName("Product Retrieval Tests")
    class RetrieveProductTests {

        @Test
        @DisplayName("Should retrieve product by ID")
        void shouldRetrieveProductById() {
            // Arrange
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(mockProductEntity));
            when(productMapper.apply(any())).thenReturn(mockProductDTO);

            // Act
            ProductDTO result = productService.getProductById(1L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return null when product not found")
        void shouldReturnNullWhenProductNotFound() {
            // Arrange
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Act
            ProductDTO result = productService.getProductById(1L);

            // Assert
            assertThat(result).isNull();
            verify(productRepository).findById(1L);
        }

        @Test
        @DisplayName("Should retrieve all active products")
        void shouldRetrieveActiveProducts() {
            // Arrange
            Page<ProductEntity> productPage = new PageImpl<>(List.of(mockProductEntity));
            when(productRepository.queryAllByActiveTrue(any())).thenReturn(productPage);
            when(productMapper.apply(any())).thenReturn(mockProductDTO);

            // Act
            Page<ProductDTO> result = productService.getActiveProducts(PageRequest.of(0, 10));

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(productRepository).queryAllByActiveTrue(any());
        }
    }

    @Nested
    @DisplayName("Product Update Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should successfully update product")
        void shouldUpdateProduct() {
            // Arrange
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(mockProductEntity));
            when(productFactory.createFromUpdateRequest(any(), any())).thenReturn(mockProductEntity);
            when(productRepository.save(any())).thenReturn(mockProductEntity);
            when(productMapper.apply(any())).thenReturn(mockProductDTO);

            // Act
            ProductUpdateResult result = productService.updateProduct(1L, validUpdateRequest);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getProduct()).isNotNull();
            assertThat(result.getWarnings()).isEmpty();
            verify(productRepository).save(any(ProductEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent product")
        void shouldThrowExceptionForNonExistentProduct() {
            // Arrange
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> productService.updateProduct(1L, validUpdateRequest))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessageContaining("Product not found with ID: 1");
        }

        @Test
        @DisplayName("Should update product price")
        void shouldUpdateProductPrice() {
            // Arrange
            PriceEntity existingPrice = PriceEntity.builder()
                    .id(1L)
                    .currency("USD")
                    .amount(99.99)
                    .active(true)
                    .build();

            // Manually set up the bidirectional relationship
            existingPrice.setProduct(mockProductEntity);
            mockProductEntity.setPrice(existingPrice);

            UpdatePriceRequest priceRequest = new UpdatePriceRequest(
                    1L,
                    "USD",
                    199.99,
                    true
            );

            when(productRepository.findById(anyLong())).thenReturn(Optional.of(mockProductEntity));
            when(productRepository.save(any())).thenReturn(mockProductEntity);
            when(productMapper.apply(any())).thenReturn(mockProductDTO);

            // Act
            ProductDTO result = productService.updateProductPrice(1L, priceRequest);

            // Assert
            assertThat(result).isNotNull();
            verify(productRepository).save(any(ProductEntity.class));
            verify(priceService).updatePrice(any(), eq(priceRequest));
        }
    }

    @Nested
    @DisplayName("Product Deletion Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Should successfully delete product")
        void shouldDeleteProduct() {
            // Arrange
            when(productRepository.findById(anyLong())).thenReturn(Optional.of(mockProductEntity));
            when(orderLineRepository.findActiveOrdersForProduct(anyLong(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            productService.deleteProduct(1L);

            // Assert
            verify(productRepository).deleteById(1L);
            verify(orderLineRepository).findActiveOrdersForProduct(anyLong(), any());
            verify(orderMapper, never()).apply(any()); // Verify mapper is never called
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent product")
        void shouldThrowExceptionForNonExistentProduct() {
            // Arrange
            when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> productService.deleteProduct(1L))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }
}