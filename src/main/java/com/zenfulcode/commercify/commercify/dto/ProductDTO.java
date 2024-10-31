package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String name;
    private String description;
    private Integer stock;
    private String stripeId;
    private Boolean active;
    private String imageUrl;
    private List<PriceDTO> prices;

    public PriceDTO getDefaultPrice() {
        return prices.stream()
                .filter(PriceDTO::getIsDefault)
                .findFirst()
                .orElse(null);
    }
}
