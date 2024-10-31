package com.zenfulcode.commercify.commercify.exception;


import com.zenfulcode.commercify.commercify.dto.ActiveOrderDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductDeletionException extends RuntimeException {
    private final List<String> issues;
    private final List<ActiveOrderDTO> activeOrders;

    public ProductDeletionException(String message, List<String> issues, List<ActiveOrderDTO> activeOrders) {
        super(message);
        this.issues = issues;
        this.activeOrders = activeOrders;
    }

}