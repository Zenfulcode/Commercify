package com.zenfulcode.commercify.web.dto.common;

import com.zenfulcode.commercify.domain.model.VariantOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for {@link VariantOption}
 */
@Builder
@Data
@AllArgsConstructor
public class VariantOptionEntityDto {
    private Long id;
    private String name;
    private String value;
}