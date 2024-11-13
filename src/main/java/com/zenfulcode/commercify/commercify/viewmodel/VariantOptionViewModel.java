package com.zenfulcode.commercify.commercify.viewmodel;

import com.zenfulcode.commercify.commercify.dto.VariantOptionEntityDto;

public record VariantOptionViewModel(
        String name,
        String value
) {
    public static VariantOptionViewModel fromDTO(VariantOptionEntityDto dto) {
        return new VariantOptionViewModel(
                dto.getName(),
                dto.getValue()
        );
    }
}