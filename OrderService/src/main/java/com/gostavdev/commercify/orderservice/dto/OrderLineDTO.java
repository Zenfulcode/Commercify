package com.gostavdev.commercify.orderservice.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderLineDTO {
    private Long productId;
    private String stripeProductId;
    private Integer quantity;
    private Double unitPrice;
    private ProductDto product;
}
