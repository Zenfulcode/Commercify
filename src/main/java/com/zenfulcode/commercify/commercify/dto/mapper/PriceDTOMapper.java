package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.PriceDTO;
import com.zenfulcode.commercify.commercify.entity.PriceEntity;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class PriceDTOMapper implements Function<PriceEntity, PriceDTO> {
    @Override
    public PriceDTO apply(PriceEntity price) {
        return PriceDTO.builder()
                .priceId(price.getPriceId())
                .currency(price.getCurrency())
                .amount(price.getAmount())
                .stripePriceId(price.getStripePriceId())
                .isDefault(price.getIsDefault())
                .active(price.getActive())
                .build();
    }
}