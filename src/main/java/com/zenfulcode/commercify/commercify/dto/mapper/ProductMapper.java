package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.ProductVariantEntityDto;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ProductMapper implements Function<ProductEntity, ProductDTO> {
    private final ProductVariantMapper variantMapper;

    @Override
    public ProductDTO apply(ProductEntity product) {
        final List<ProductVariantEntityDto> variants = product.getVariants().stream().map(variantMapper).toList();

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .stock(product.getStock())
                .active(product.getActive())
                .imageUrl(product.getImageUrl())
                .unitPrice(product.getUnitPrice())
                .currency(product.getCurrency())
                .variants(variants)
                .build();
    }
}
