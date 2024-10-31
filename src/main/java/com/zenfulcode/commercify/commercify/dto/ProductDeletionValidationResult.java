package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductDeletionValidationResult {
    private boolean canDelete;
    private List<String> issues;
    private List<ActiveOrderDTO> activeOrders;

    public boolean canDelete() {
        return activeOrders.isEmpty() && issues.isEmpty();
    }
}