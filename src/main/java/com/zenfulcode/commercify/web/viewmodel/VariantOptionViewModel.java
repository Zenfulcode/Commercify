package com.zenfulcode.commercify.web.viewmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zenfulcode.commercify.web.dto.common.VariantOptionEntityDto;

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