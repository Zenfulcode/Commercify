package com.zenfulcode.commercify.order.application.dto;

import com.zenfulcode.commercify.order.domain.model.OrderLine;
import com.zenfulcode.commercify.order.domain.valueobject.OrderLineId;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantId;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.Builder;

@Builder
public record OrderLineDTO(OrderLineId id,
                           ProductId productId,
                           VariantId variantId,
                           int quantity,
                           Money unitPrice,
                           Money total,
                           String variantSku) {
    public static OrderLineDTO fromOrderLine(OrderLine orderLine) {
        return OrderLineDTO.builder()
                .id(orderLine.getId())
                .productId(orderLine.getProductId())
                .variantId(orderLine.getProductVariant() != null ?
                        orderLine.getProductVariant().getId() : null)
                .quantity(orderLine.getQuantity())
                .unitPrice(orderLine.getUnitPrice())
                .total(orderLine.getTotal())
                .variantSku(orderLine.getProductVariant() != null ?
                        orderLine.getProductVariant().getSku() : null)
                .build();
    }
}
