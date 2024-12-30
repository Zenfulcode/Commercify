package com.zenfulcode.commercify.factory;

import com.zenfulcode.commercify.component.factory.ProductFactory;
import com.zenfulcode.commercify.web.dto.request.product.PriceRequest;
import com.zenfulcode.commercify.web.dto.request.product.ProductRequest;
import com.zenfulcode.commercify.domain.model.Product;
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
        ProductRequest updateRequest = new ProductRequest(
                "Updated Product",
                "Updated Description",
                20,
                "updated-image.jpg",
                true,
                priceRequest,
                new ArrayList<>()
        );
    }

    @Nested
    @DisplayName("Create Product Tests")
    class CreateProductTests {

        @Test
        @DisplayName("Should create product from request")
        void testCreateFromRequest() {
            Product result = productFactory.createFromRequest(createRequest);

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

            Product result = productFactory.createFromRequest(requestWithNullStock);

            assertNotNull(result);
            assertEquals(0, result.getStock());
        }

        @Test
        @DisplayName("Should preserve unitPrice information in create request")
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

            Product result = productFactory.createFromRequest(requestWithDifferentPrice);

            assertEquals("EUR", result.getCurrency());
            assertEquals(149.99, result.getUnitPrice());
        }
    }
}