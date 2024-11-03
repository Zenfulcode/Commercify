package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.dto.PriceDTO;

public record PriceViewModel(
        String currency,
        Double amount
) {
    public static PriceViewModel fromDTO(PriceDTO priceDTO) {
        return new PriceViewModel(
                priceDTO.getCurrency(),
                priceDTO.getAmount()
        );
    }

    public static PriceViewModel from(String currency, Double amount) {
        return new PriceViewModel(
                currency,
                amount
        );
    }
}
