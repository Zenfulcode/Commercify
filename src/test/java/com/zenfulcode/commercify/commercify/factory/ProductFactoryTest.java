package com.zenfulcode.commercify.commercify.factory;

import com.zenfulcode.commercify.commercify.api.requests.products.CreatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.CreateProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdateProductRequest;
import com.zenfulcode.commercify.commercify.entity.PriceEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Product Factory Tests")
class ProductFactoryTest {

    private final ProductFactory productFactory = new ProductFactory();

    @Nested
    @DisplayName("Create From Request Tests")
    class CreateFromRequestTests {

        @Test
        @DisplayName("Should create product from complete request")
        void shouldCreateFromCompleteRequest() {
            // Arrange
            CreateProductRequest request = new CreateProductRequest(
                    "Test Product",
                    "Test Description",
                    10,
                    "test-image.jpg",
                    true,
                    new CreatePriceRequest("USD", 100d, true)
            );

            // Act
            ProductEntity result = productFactory.createFromRequest(request);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .satisfies(product -> {
                        assertThat(product.getName()).isEqualTo("Test Product");
                        assertThat(product.getDescription()).isEqualTo("Test Description");
                        assertThat(product.getStock()).isEqualTo(10);
                        assertThat(product.getActive()).isTrue();
                        assertThat(product.getImageUrl()).isEqualTo("test-image.jpg");
                        assertThat(product.getPrice()).isNotNull();
                    });
        }

        @Test
        @DisplayName("Should handle null stock value")
        void shouldHandleNullStock() {
            // Arrange
            CreateProductRequest request = new CreateProductRequest(
                    "Test Product",
                    "Test Description",
                    null,
                    "test-image.jpg",
                    true,
                    new CreatePriceRequest("USD", 100d, true)
            );

            // Act
            ProductEntity result = productFactory.createFromRequest(request);

            // Assert
            assertThat(result.getStock()).isZero();
        }

        @Test
        @DisplayName("Should initialize empty prices list")
        void shouldInitializeEmptyPricesList() {
            // Arrange
            CreateProductRequest request = new CreateProductRequest(
                    "Test Product",
                    "Test Description",
                    10,
                    "test-image.jpg",
                    true,
                    new CreatePriceRequest("USD", 100d, true)
            );

            // Act
            ProductEntity result = productFactory.createFromRequest(request);

            // Assert
            assertThat(result.getPrice()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Create From Update Request Tests")
    class CreateFromUpdateRequestTests {

        @Test
        @DisplayName("Should update all provided fields")
        void shouldUpdateAllProvidedFields() {
            // Arrange
            ProductEntity existingProduct = ProductEntity.builder()
                    .id(1L)
                    .name("Original Name")
                    .description("Original Description")
                    .stock(5)
                    .active(false)
                    .imageUrl("original-image.jpg")
                    .stripeId("stripe_123")
                    .price(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            UpdateProductRequest request = new UpdateProductRequest(
                    "Updated Name",
                    "Updated Description",
                    10,
                    "updated-image.jpg",
                    true,
                    null
            );

            // Act
            ProductEntity result = productFactory.createFromUpdateRequest(request, existingProduct);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .satisfies(product -> {
                        assertThat(product.getId()).isEqualTo(existingProduct.getId());
                        assertThat(product.getName()).isEqualTo("Updated Name");
                        assertThat(product.getDescription()).isEqualTo("Updated Description");
                        assertThat(product.getStock()).isEqualTo(10);
                        assertThat(product.getActive()).isTrue();
                        assertThat(product.getImageUrl()).isEqualTo("updated-image.jpg");
                        assertThat(product.getStripeId()).isEqualTo(existingProduct.getStripeId());
                        assertThat(product.getCreatedAt()).isEqualTo(existingProduct.getCreatedAt());
                    });
        }

        @Test
        @DisplayName("Should retain existing values for null fields")
        void shouldRetainExistingValuesForNullFields() {
            // Arrange
            ProductEntity existingProduct = ProductEntity.builder()
                    .id(1L)
                    .name("Original Name")
                    .description("Original Description")
                    .stock(5)
                    .active(false)
                    .imageUrl("original-image.jpg")
                    .stripeId("stripe_123")
                    .price(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            UpdateProductRequest request = new UpdateProductRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // Act
            ProductEntity result = productFactory.createFromUpdateRequest(request, existingProduct);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .satisfies(product -> {
                        assertThat(product.getName()).isEqualTo(existingProduct.getName());
                        assertThat(product.getDescription()).isEqualTo(existingProduct.getDescription());
                        assertThat(product.getStock()).isEqualTo(existingProduct.getStock());
                        assertThat(product.getActive()).isEqualTo(existingProduct.getActive());
                        assertThat(product.getImageUrl()).isEqualTo(existingProduct.getImageUrl());
                    });
        }

        @Test
        @DisplayName("Should copy prices list")
        void shouldCopyPrice() {
            // Arrange
            PriceEntity existingPrice = PriceEntity.builder().id(1L).build();

            ProductEntity existingProduct = ProductEntity.builder()
                    .id(1L)
                    .name("Original Name")
                    .price(existingPrice)
                    .build();

            UpdateProductRequest request = new UpdateProductRequest(
                    "Updated Name",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // Act
            ProductEntity result = productFactory.createFromUpdateRequest(request, existingProduct);

            // Assert
            assertThat(result.getPrice())
                    .isNotNull()
                    .isNotSameAs(existingProduct.getPrice());
        }
    }
}