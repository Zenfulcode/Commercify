package com.zenfulcode.commercify.commercify.factory;

import com.zenfulcode.commercify.commercify.api.requests.products.ProductRequest;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductFactory {
    public ProductEntity createFromRequest(ProductRequest request) {
        return ProductEntity.builder()
                .name(request.name())
                .description(request.description())
                .stock(request.stock() != null ? request.stock() : 0)
                .active(true)
                .imageUrl(request.imageUrl())
                .currency(request.price().currency())
                .unitPrice(request.price().amount())
                .build();
    }
}

