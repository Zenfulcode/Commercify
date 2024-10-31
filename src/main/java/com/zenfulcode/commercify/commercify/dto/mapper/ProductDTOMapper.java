package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ProductDTOMapper implements Function<ProductEntity, ProductDTO> {
    @Override
    public ProductDTO apply(ProductEntity product) {
        return ProductDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .currency(product.getCurrency())
                .unitPrice(product.getUnitPrice())
                .stock(product.getStock())
                .stripeId(product.getStripeId())
                .active(product.getActive())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
