package com.zenfulcode.commercify.commercify.api.responses;

import com.zenfulcode.commercify.commercify.dto.ActiveOrderDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductDeletionErrorResponse {
    private String message;
    private List<String> issues;
    private List<ActiveOrderDTO> activeOrders;
}
