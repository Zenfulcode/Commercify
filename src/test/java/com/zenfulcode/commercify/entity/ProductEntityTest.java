package com.zenfulcode.commercify.entity;


import com.zenfulcode.commercify.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductEntityTest {

    private Product product;

    @BeforeEach
    void setUp() {
        product = com.zenfulcode.commercify.domain.model.Product.builder()
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
        Product emptyProduct = new Product();
        assertNull(emptyProduct.getId());
        assertNull(emptyProduct.getName());
        assertNull(emptyProduct.getStock());
        assertNull(emptyProduct.getActive());
    }
}