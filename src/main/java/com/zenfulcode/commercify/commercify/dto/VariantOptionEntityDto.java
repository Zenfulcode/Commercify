package com.zenfulcode.commercify.commercify.dto;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.zenfulcode.commercify.commercify.entity.VariantOptionEntity}
 */
@Value
@Builder
public class VariantOptionEntityDto implements Serializable {
    Long id;
    String name;
    String value;
}