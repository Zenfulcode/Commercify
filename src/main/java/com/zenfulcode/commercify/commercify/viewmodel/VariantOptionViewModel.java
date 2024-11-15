package com.zenfulcode.commercify.commercify.viewmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zenfulcode.commercify.commercify.dto.VariantOptionEntityDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
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