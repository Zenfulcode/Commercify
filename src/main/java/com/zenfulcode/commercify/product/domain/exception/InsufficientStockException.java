package com.zenfulcode.commercify.product.domain.exception;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.shared.domain.exception.DomainException;
import lombok.Getter;

@Getter
public class InsufficientStockException extends DomainException {
    private final ProductId productId;
    private final int requestedQuantity;
    private final int availableStock;

    public InsufficientStockException(ProductId productId, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
                productId, requestedQuantity, availableStock));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }
}
