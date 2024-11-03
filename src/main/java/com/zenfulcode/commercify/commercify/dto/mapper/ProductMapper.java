package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ProductMapper implements Function<ProductEntity, ProductDTO> {

    @Override
    public ProductDTO apply(ProductEntity product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .stock(product.getStock())
                .stripeId(product.getStripeId())
                .active(product.getActive())
                .imageUrl(product.getImageUrl())
                .unitPrice(product.getUnitPrice())
                .currency(product.getCurrency())
                .stripePriceId(product.getStripePriceId())
                .build();
    }
}
