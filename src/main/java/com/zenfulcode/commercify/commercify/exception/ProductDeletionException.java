package com.zenfulcode.commercify.commercify.exception;


import com.zenfulcode.commercify.commercify.dto.ActiveOrderDTO;

import java.util.List;

// ProductDeletionException.java
public class ProductDeletionException extends RuntimeException {
    private final List<String> issues;
    private final List<ActiveOrderDTO> activeOrders;

    public ProductDeletionException(String message, List<String> issues, List<ActiveOrderDTO> activeOrders) {
        super(message);
        this.issues = issues;
        this.activeOrders = activeOrders;
    }

    public List<String> getIssues() {
        return issues;
    }

    public List<ActiveOrderDTO> getActiveOrders() {
        return activeOrders;
    }
}