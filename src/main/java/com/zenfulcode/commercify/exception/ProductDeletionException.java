package com.zenfulcode.commercify.exception;

import com.zenfulcode.commercify.web.dto.common.OrderDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class ProductDeletionException extends RuntimeException {
    private final List<String> issues;
    private final List<OrderDTO> activeOrders;

    public ProductDeletionException(String message, List<String> issues, List<OrderDTO> activeOrders) {
        super(message);
        this.issues = issues;
        this.activeOrders = activeOrders;
    }
}