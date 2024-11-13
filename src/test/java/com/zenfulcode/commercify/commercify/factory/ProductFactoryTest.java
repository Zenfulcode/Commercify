package com.zenfulcode.commercify.commercify.factory;

import com.zenfulcode.commercify.commercify.api.requests.products.PriceRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.ProductRequest;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductFactoryTest {

    @InjectMocks
    private ProductFactory productFactory;

    private ProductRequest createRequest;
    private ProductRequest updateRequest;
    private ProductEntity existingProduct;

    @BeforeEach
    void setUp() {
        PriceRequest createPriceRequest = new PriceRequest("USD", 99.99);
        createRequest = new ProductRequest(
                "Test Product",
                "Test Description",
                10,
                "test-image.jpg",
                true,
                createPriceRequest,
                new ArrayList<>()
        );

        PriceRequest priceRequest = new PriceRequest(
                "USD",
                99.99
        );
        updateRequest = new ProductRequest(
                "Updated Product",
                "Updated Description",
                20,
                "updated-image.jpg",
                true,
                priceRequest,
                new ArrayList<>()
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
            ProductRequest requestWithNullStock = new ProductRequest(
                    "Test Product",
                    "Test Description",
                    null,
                    "test-image.jpg",
                    true,
                    new PriceRequest("USD", 99.99),
                    new ArrayList<>()
            );

            ProductEntity result = productFactory.createFromRequest(requestWithNullStock);

            assertNotNull(result);
            assertEquals(0, result.getStock());
        }

        @Test
        @DisplayName("Should preserve price information in create request")
        void testCreateFromRequestPriceInfo() {
            PriceRequest priceRequest = new PriceRequest("EUR", 149.99);
            ProductRequest requestWithDifferentPrice = new ProductRequest(
                    "Test Product",
                    "Test Description",
                    10,
                    "test-image.jpg",
                    true,
                    priceRequest,
                    new ArrayList<>()
            );

            ProductEntity result = productFactory.createFromRequest(requestWithDifferentPrice);

            assertEquals("EUR", result.getCurrency());
            assertEquals(149.99, result.getUnitPrice());
        }
    }
}