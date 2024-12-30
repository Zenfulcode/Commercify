package com.zenfulcode.commercify.component.factory;

import com.zenfulcode.commercify.web.dto.request.product.ProductRequest;
import com.zenfulcode.commercify.domain.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductFactory {
    public Product createFromRequest(ProductRequest request) {
        return com.zenfulcode.commercify.domain.model.Product.builder()
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

