package com.zenfulcode.commercify.commercify.factory;

import com.zenfulcode.commercify.commercify.api.requests.products.CreateProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdateProductRequest;
import com.zenfulcode.commercify.commercify.entity.PriceEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                    List.of()
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
                        assertThat(product.getPrices()).isEmpty();
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
                    List.of()
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
                    List.of()
            );

            // Act
            ProductEntity result = productFactory.createFromRequest(request);

            // Assert
            assertThat(result.getPrices())
                    .isNotNull()
                    .isEmpty();
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
                    .prices(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .build();

            UpdateProductRequest request = new UpdateProductRequest(
                    "Updated Name",
                    "Updated Description",
                    10,
                    "updated-image.jpg",
                    true,
                    List.of()
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
                    .prices(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .build();

            UpdateProductRequest request = new UpdateProductRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of()
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
        void shouldCopyPricesList() {
            // Arrange
            List<PriceEntity> existingPrices = new ArrayList<>();
            existingPrices.add(PriceEntity.builder().id(1L).build());

            ProductEntity existingProduct = ProductEntity.builder()
                    .id(1L)
                    .name("Original Name")
                    .prices(existingPrices)
                    .build();

            UpdateProductRequest request = new UpdateProductRequest(
                    "Updated Name",
                    null,
                    null,
                    null,
                    null,
                    List.of()
            );

            // Act
            ProductEntity result = productFactory.createFromUpdateRequest(request, existingProduct);

            // Assert
            assertThat(result.getPrices())
                    .isNotNull()
                    .hasSize(1)
                    .isNotSameAs(existingProduct.getPrices());
        }
    }

    @Nested
    @DisplayName("Duplicate Tests")
    class DuplicateTests {

        @Test
        @DisplayName("Should create duplicate product with modified name")
        void shouldCreateDuplicateWithModifiedName() {
            // Arrange
            ProductEntity original = ProductEntity.builder()
                    .name("Original Product")
                    .description("Test Description")
                    .stock(10)
                    .active(true)
                    .imageUrl("test-image.jpg")
                    .prices(new ArrayList<>())
                    .build();

            // Act
            ProductEntity result = productFactory.duplicate(original);

            // Assert
            assertThat(result)
                    .isNotNull()
                    .satisfies(product -> {
                        assertThat(product.getName()).isEqualTo("Original Product (Copy)");
                        assertThat(product.getDescription()).isEqualTo(original.getDescription());
                        assertThat(product.getStock()).isZero();
                        assertThat(product.getActive()).isFalse();
                        assertThat(product.getImageUrl()).isEqualTo(original.getImageUrl());
                        assertThat(product.getPrices()).isEmpty();
                    });
        }

        @Test
        @DisplayName("Should initialize new prices list for duplicate")
        void shouldInitializeNewPricesListForDuplicate() {
            // Arrange
            List<PriceEntity> originalPrices = new ArrayList<>();
            originalPrices.add(PriceEntity.builder().id(1L).build());

            ProductEntity original = ProductEntity.builder()
                    .name("Original Product")
                    .prices(originalPrices)
                    .build();

            // Act
            ProductEntity result = productFactory.duplicate(original);

            // Assert
            assertThat(result.getPrices())
                    .isNotNull()
                    .isNotSameAs(originalPrices)
                    .isEmpty();
        }
    }
}