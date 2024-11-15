package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.VariantOptionEntityDto;
import com.zenfulcode.commercify.commercify.entity.VariantOptionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class VariantOptionMapper implements Function<VariantOptionEntity, VariantOptionEntityDto> {

    @Override
    public VariantOptionEntityDto apply(VariantOptionEntity product) {
        return VariantOptionEntityDto.builder()
                .id(product.getId())
                .name(product.getName())
                .value(product.getValue())
                .build();
    }
}