package com.zenfulcode.commercify.commercify.factory;

import com.zenfulcode.commercify.commercify.api.requests.products.CreatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.CreateProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdatePriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdateProductRequest;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductFactoryTest {

    @InjectMocks
    private ProductFactory productFactory;

    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;
    private ProductEntity existingProduct;

    @BeforeEach
    void setUp() {
        CreatePriceRequest createPriceRequest = new CreatePriceRequest("USD", 99.99);
        createRequest = new CreateProductRequest(
                "Test Product",
                "Test Description",
                10,
                "test-image.jpg",
                true,
                createPriceRequest
        );

        UpdatePriceRequest updatePriceRequest = new UpdatePriceRequest(
                1L,
                "USD",
                99.99
        );
        updateRequest = new UpdateProductRequest(
                "Updated Product",
                "Updated Description",
                20,
                "updated-image.jpg",
                true,
                updatePriceRequest
        );

        existingProduct = ProductEntity.builder()
                .id(1L)
                .name("Existing Product")
                .description("Existing Description")
                .stock(5)
                .active(true)
                .imageUrl("existing-image.jpg")
                .currency("USD")
                .unitPrice(79.99)
                .stripeId("stripe_prod_123")
                .stripePriceId("stripe_price_123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product from request")
        void testCreateFromRequest() {
            ProductEntity result = productFactory.createFromRequest(createRequest);

            assertNotNull(result);
            assertEquals("Test Product", result.getName());
            assertEquals("Test Description", result.getDescription());
            assertEquals(10, result.getStock());
            assertTrue(result.getActive());
            assertEquals("test-image.jpg", result.getImageUrl());
            assertEquals("USD", result.getCurrency());
            assertEquals(99.99, result.getUnitPrice());
        }

        @Test
        @DisplayName("Should handle null stock in create request")
        void testCreateFromRequestWithNullStock() {
            CreateProductRequest requestWithNullStock = new CreateProductRequest(
                    "Test Product",
                    "Test Description",
                    null,
                    "test-image.jpg",
                    true,
                    new CreatePriceRequest("USD", 99.99)
            );

            ProductEntity result = productFactory.createFromRequest(requestWithNullStock);

            assertNotNull(result);
            assertEquals(0, result.getStock());
        }

        @Test
        @DisplayName("Should preserve price information in create request")
        void testCreateFromRequestPriceInfo() {
            CreatePriceRequest priceRequest = new CreatePriceRequest("EUR", 149.99);
            CreateProductRequest requestWithDifferentPrice = new CreateProductRequest(
                    "Test Product",
                    "Test Description",
                    10,
                    "test-image.jpg",
                    true,
                    priceRequest
            );

            ProductEntity result = productFactory.createFromRequest(requestWithDifferentPrice);

            assertEquals("EUR", result.getCurrency());
            assertEquals(149.99, result.getUnitPrice());
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Should update all fields when all are provided")
        void testUpdateFromRequestAllFields() {
            ProductEntity result = productFactory.createFromUpdateRequest(updateRequest, existingProduct);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("Updated Product", result.getName());
            assertEquals("Updated Description", result.getDescription());
            assertEquals(20, result.getStock());
            assertEquals("updated-image.jpg", result.getImageUrl());
            assertEquals("USD", result.getCurrency());
            assertEquals(99.99, result.getUnitPrice());
            // Verify stripe IDs are preserved
            assertEquals("stripe_prod_123", result.getStripeId());
            assertEquals("stripe_price_123", result.getStripePriceId());
            // Verify timestamps are preserved
            assertEquals(existingProduct.getCreatedAt(), result.getCreatedAt());
            assertEquals(existingProduct.getUpdatedAt(), result.getUpdatedAt());
        }

        @Test
        @DisplayName("Should preserve existing fields when update fields are null")
        void testUpdateFromRequestWithNullFields() {
            UpdatePriceRequest updatePriceRequest = new UpdatePriceRequest(1L, null, null);
            UpdateProductRequest nullFieldsRequest = new UpdateProductRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    updatePriceRequest
            );

            ProductEntity result = productFactory.createFromUpdateRequest(nullFieldsRequest, existingProduct);

            assertNotNull(result);
            assertEquals(existingProduct.getName(), result.getName());
            assertEquals(existingProduct.getDescription(), result.getDescription());
            assertEquals(existingProduct.getStock(), result.getStock());
            assertEquals(existingProduct.getImageUrl(), result.getImageUrl());
            assertEquals(existingProduct.getCurrency(), result.getCurrency());
            assertEquals(existingProduct.getUnitPrice(), result.getUnitPrice());
        }

        @Test
        @DisplayName("Should handle partial updates")
        void testPartialUpdate() {
            UpdatePriceRequest updatePriceRequest = new UpdatePriceRequest(1L, "EUR", null);
            UpdateProductRequest partialRequest = new UpdateProductRequest(
                    "Updated Product",
                    null,
                    15,
                    null,
                    null,
                    updatePriceRequest
            );

            ProductEntity result = productFactory.createFromUpdateRequest(partialRequest, existingProduct);

            assertEquals("Updated Product", result.getName());
            assertEquals(existingProduct.getDescription(), result.getDescription());
            assertEquals(15, result.getStock());
            assertEquals(existingProduct.getImageUrl(), result.getImageUrl());
            assertEquals("EUR", result.getCurrency());
            assertEquals(existingProduct.getUnitPrice(), result.getUnitPrice());
        }

        @Test
        @DisplayName("Should preserve metadata during update")
        void testPreserveMetadata() {
            ProductEntity result = productFactory.createFromUpdateRequest(updateRequest, existingProduct);

            assertEquals(existingProduct.getStripeId(), result.getStripeId());
            assertEquals(existingProduct.getStripePriceId(), result.getStripePriceId());
            assertEquals(existingProduct.getCreatedAt(), result.getCreatedAt());
            assertEquals(existingProduct.getUpdatedAt(), result.getUpdatedAt());
        }
    }
}