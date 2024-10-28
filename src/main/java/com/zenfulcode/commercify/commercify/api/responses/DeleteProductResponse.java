package com.zenfulcode.commercify.commercify.api.responses;

public record DeleteProductResponse(boolean deleted, String message) {
    public DeleteProductResponse(boolean deleted) {
        this(deleted, deleted ? "Product deleted successfully" : "Product deactivated successfully");
    }
}
