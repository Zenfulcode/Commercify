package com.zenfulcode.commercify.shared.infrastructure.persistence;

import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductIdConverter implements AttributeConverter<ProductId, String> {

    @Override
    public String convertToDatabaseColumn(ProductId productId) {
        return productId == null ? null : productId.getValue();
    }

    @Override
    public ProductId convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ProductId.of(dbData);
    }
}
