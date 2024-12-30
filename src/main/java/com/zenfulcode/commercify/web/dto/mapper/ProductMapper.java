package com.zenfulcode.commercify.web.dto.mapper;

import com.zenfulcode.commercify.domain.model.Product;
import com.zenfulcode.commercify.web.dto.common.ProductDTO;
import com.zenfulcode.commercify.web.dto.common.ProductVariantEntityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ProductMapper implements Function<Product, ProductDTO> {
    private final ProductVariantMapper variantMapper;

    @Override
    public ProductDTO apply(Product product) {
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
