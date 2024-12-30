package com.zenfulcode.commercify.commercify.entity;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductEntityTest {

    private ProductEntity product;

    @BeforeEach
    void setUp() {
        product = ProductEntity.builder()
                .id(1)
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
    @DisplayName("Should create product with builder pattern")
    void testProductBuilder() {
        assertNotNull(product);
        assertEquals("Test Product", product.getName());
        assertEquals("Test Description", product.getDescription());
        assertEquals(10, product.getStock());
        assertTrue(product.getActive());
        assertEquals("USD", product.getCurrency());
        assertEquals(99.99, product.getUnitPrice());
    }

    @Test
    @DisplayName("Should update product fields")
    void testProductUpdate() {
        product.setName("Updated Product");
        product.setStock(5);
        product.setUnitPrice(149.99);

        assertEquals("Updated Product", product.getName());
        assertEquals(5, product.getStock());
        assertEquals(149.99, product.getUnitPrice());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        ProductEntity emptyProduct = new ProductEntity();
        assertNull(emptyProduct.getId());
        assertNull(emptyProduct.getName());
        assertNull(emptyProduct.getStock());
        assertNull(emptyProduct.getActive());
    }
}