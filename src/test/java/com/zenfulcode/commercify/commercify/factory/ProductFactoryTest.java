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
}