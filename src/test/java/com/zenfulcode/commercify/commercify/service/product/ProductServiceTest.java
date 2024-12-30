package com.zenfulcode.commercify.commercify.service.product;

import com.zenfulcode.commercify.commercify.api.requests.products.PriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.ProductRequest;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.factory.ProductFactory;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
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
    private ProductMapper productMapper;
    @Mock
    private ProductFactory productFactory;
    @Mock
    private ProductValidationService validationService;
    @Mock
    private ProductVariantService variantService;
    
    @InjectMocks
    private ProductService productService;

    private ProductEntity productEntity;
    private ProductDTO productDTO;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        productEntity = ProductEntity.builder()
                .id(1)
                .name("Test Product")
                .description("Test Description")
                .stock(10)
                .active(true)
                .imageUrl("test-image.jpg")
                .currency("USD")
                .unitPrice(99.99)
                .build();

        productDTO = ProductDTO.builder()
                .id(1)
                .name("Test Product")
                .description("Test Description")
                .stock(10)
                .active(true)
                .imageUrl("test-image.jpg")
                .currency("USD")
                .unitPrice(99.99)
                .variants(new ArrayList<>())
                .build();

        productRequest = new ProductRequest(
                "Test Product",
                "Test Description",
                10,
                "test-image.jpg",
                true,
                new PriceRequest("USD", 99.99),
                new ArrayList<>()
        );
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product successfully")
        void createProduct_Success() {
            when(productFactory.createFromRequest(any())).thenReturn(productEntity);
            when(productRepository.save(any())).thenReturn(productEntity);
            when(productMapper.apply(any())).thenReturn(productDTO);

            ProductDTO result = productService.saveProduct(productRequest);

            assertNotNull(result);
            assertEquals(productDTO.getName(), result.getName());
            assertEquals(productDTO.getUnitPrice(), result.getUnitPrice());
            verify(validationService).validateProductRequest(productRequest);
        }
    }

    @Nested
    @DisplayName("Retrieve Product Tests")
    class RetrieveProductTests {

        @Test
        @DisplayName("Should get product by ID")
        void getProductById_Success() {
            when(productRepository.findById(1)).thenReturn(Optional.of(productEntity));
            when(productMapper.apply(productEntity)).thenReturn(productDTO);

            ProductDTO result = productService.getProductById(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Test Product", result.getName());
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void getProductById_NotFound() {
            when(productRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () -> productService.getProductById(1));
        }

        @Test
        @DisplayName("Should get all active products")
        void getActiveProducts_Success() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            List<ProductEntity> products = List.of(productEntity);
            Page<ProductEntity> productPage = new PageImpl<>(products);

            when(productRepository.queryAllByActiveTrue(pageRequest)).thenReturn(productPage);
            when(productMapper.apply(any())).thenReturn(productDTO);

            Page<ProductDTO> result = productService.getActiveProducts(pageRequest);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(productRepository).queryAllByActiveTrue(pageRequest);
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update product successfully")
        void updateProduct_Success() {
            when(productRepository.findById(1)).thenReturn(Optional.of(productEntity));
            when(productRepository.save(any())).thenReturn(productEntity);
            when(productMapper.apply(any())).thenReturn(productDTO);

            var result = productService.updateProduct(1, productRequest);

            assertNotNull(result);
            assertEquals(productDTO, result.getProduct());
            assertTrue(result.getWarnings().isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent product")
        void updateProduct_NotFound() {
            when(productRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class,
                    () -> productService.updateProduct(1, productRequest));
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {
        @Mock
        private ProductDeletionService productDeletionService;

        @BeforeEach
        void setUp() {
            productService = new ProductService(
                    productRepository,
                    productMapper,
                    productFactory,
                    validationService,
                    variantService,
                    productDeletionService
            );
        }

        @Test
        @DisplayName("Should delete product successfully")
        void deleteProduct_Success() {
            when(productRepository.findById(1)).thenReturn(Optional.of(productEntity));
            doNothing().when(productDeletionService).validateAndDelete(productEntity);

            assertDoesNotThrow(() -> productService.deleteProduct(1));
            verify(productDeletionService).validateAndDelete(productEntity);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent product")
        void deleteProduct_NotFound() {
            when(productRepository.findById(1)).thenReturn(Optional.empty());

            assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(1));
            verify(productDeletionService, never()).validateAndDelete(any());
        }
    }

    @Nested
    @DisplayName("Product Status Tests")
    class ProductStatusTests {

        @Test
        @DisplayName("Should activate product")
        void activateProduct_Success() {
            when(productRepository.findById(1)).thenReturn(Optional.of(productEntity));

            assertDoesNotThrow(() -> productService.reactivateProduct(1));
            assertTrue(productEntity.getActive());
            verify(productRepository).save(productEntity);
        }

        @Test
        @DisplayName("Should deactivate product")
        void deactivateProduct_Success() {
            when(productRepository.findById(1)).thenReturn(Optional.of(productEntity));

            assertDoesNotThrow(() -> productService.deactivateProduct(1));
            assertFalse(productEntity.getActive());
            verify(productRepository).save(productEntity);
        }
    }
}