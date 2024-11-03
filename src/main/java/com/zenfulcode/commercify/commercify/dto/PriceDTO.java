package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PriceDTO {
    private Long id;
    private String currency;
    private Double amount;
    private String stripePriceId;
    private Boolean active;
}
