package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductDTOMapper implements Function<ProductEntity, ProductDTO> {
    private final PriceDTOMapper priceDTOMapper;

    @Override
    public ProductDTO apply(ProductEntity product) {
        return ProductDTO.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .stock(product.getStock())
                .stripeId(product.getStripeId())
                .active(product.getActive())
                .imageUrl(product.getImageUrl())
                .prices(product.getPrices().stream()
                        .map(priceDTOMapper)
                        .collect(Collectors.toList()))
                .build();
    }
}
