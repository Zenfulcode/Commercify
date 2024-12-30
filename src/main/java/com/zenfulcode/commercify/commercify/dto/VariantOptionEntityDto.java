package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link com.zenfulcode.commercify.commercify.entity.VariantOptionEntity}
 */
@Builder
@Data
@AllArgsConstructor
public class VariantOptionEntityDto {
    private Integer id;
    private String name;
    private String value;
}