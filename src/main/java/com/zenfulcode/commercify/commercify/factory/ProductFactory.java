package com.zenfulcode.commercify.commercify.factory;

import com.zenfulcode.commercify.commercify.api.requests.products.CreateProductRequest;
import com.zenfulcode.commercify.commercify.api.requests.products.UpdateProductRequest;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Slf4j
public class ProductFactory {
    public ProductEntity createFromRequest(CreateProductRequest request) {
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

    public ProductEntity createFromUpdateRequest(UpdateProductRequest request, ProductEntity existingProduct) {
        ProductEntity.ProductEntityBuilder builder = ProductEntity.builder()
                .id(existingProduct.getId())
                .name(request.name() != null ? request.name() : existingProduct.getName())
                .description(request.description() != null ? request.description() : existingProduct.getDescription())
                .stock(request.stock() != null ? request.stock() : existingProduct.getStock())
                .active(request.active() != null ? request.active() : existingProduct.getActive())
                .imageUrl(request.imageUrl() != null ? request.imageUrl() : existingProduct.getImageUrl())
                .currency(request.price().currency() != null ? request.price().currency() : existingProduct.getCurrency())
                .unitPrice(request.price().amount() != null ? request.price().amount() : existingProduct.getUnitPrice())
                .stripeId(existingProduct.getStripeId())
                .stripePriceId(existingProduct.getStripePriceId())
                .createdAt(existingProduct.getCreatedAt())
                .updatedAt(existingProduct.getUpdatedAt());

        return builder.build();
    }

}

