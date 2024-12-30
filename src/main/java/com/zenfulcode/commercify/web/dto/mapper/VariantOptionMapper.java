package com.zenfulcode.commercify.web.dto.mapper;

import com.zenfulcode.commercify.web.dto.common.VariantOptionEntityDto;
import com.zenfulcode.commercify.domain.model.VariantOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class VariantOptionMapper implements Function<VariantOption, VariantOptionEntityDto> {

    @Override
    public VariantOptionEntityDto apply(VariantOption product) {
        return VariantOptionEntityDto.builder()
                .id(product.getId())
                .name(product.getName())
                .value(product.getValue())
                .build();
    }
}