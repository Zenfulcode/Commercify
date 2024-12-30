package com.zenfulcode.commercify.web.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductDeletionValidationResult {
    private boolean canDelete;
    private List<String> issues;
    private List<OrderDTO> activeOrders;

    public boolean canDelete() {
        return activeOrders.isEmpty() && issues.isEmpty();
    }
}