package com.zenfulcode.commercify.api.product.mapper;

import com.zenfulcode.commercify.api.product.dto.request.*;
import com.zenfulcode.commercify.api.product.dto.response.ProductDetailResponse;
import com.zenfulcode.commercify.api.product.dto.response.ProductVariantSummaryResponse;
import com.zenfulcode.commercify.product.application.command.*;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductDtoMapper {

    public CreateProductCommand toCommand(CreateProductRequest request) {
        return new CreateProductCommand(
                request.name(),
                request.description(),
                request.imageUrl(),
                request.initialStock(),
                request.price(),
                mapVariantSpecs(request.variants())
        );
    }

    public AddProductVariantsCommand toCommand(ProductId productId, AddVariantsRequest request) {
        List<VariantSpecification> specs = request.variants().stream()
                .map(this::toVariantSpec)
                .collect(Collectors.toList());

        return new AddProductVariantsCommand(productId, specs);
    }

    public AdjustInventoryCommand toCommand(ProductId productId, AdjustInventoryRequest request) {
        return new AdjustInventoryCommand(
                productId,
                InventoryAdjustmentType.valueOf(request.type()),
                request.quantity(),
                request.reason()
        );
    }

    public UpdateProductCommand toCommand(ProductId productId, UpdateProductRequest request) {
        ProductUpdateSpec updateSpec = new ProductUpdateSpec(
                request.name(),
                request.description(),
                request.imageUrl(),
                request.stock(),
                request.price(),
                request.active()
        );
        return new UpdateProductCommand(productId, updateSpec);
    }

    public UpdateVariantPricesCommand toCommand(ProductId productId, UpdateVariantPricesRequest request) {
        List<VariantPriceUpdate> updates = request.updates().stream()
                .map(update -> new VariantPriceUpdate(
                        update.sku(),
                        update.price()
                ))
                .collect(Collectors.toList());

        return new UpdateVariantPricesCommand(productId, updates);
    }

    private List<VariantSpecification> mapVariantSpecs(List<CreateVariantRequest> variants) {
        if (variants == null) return List.of();

        return variants.stream()
                .map(this::toVariantSpec)
                .collect(Collectors.toList());
    }

    private VariantSpecification toVariantSpec(CreateVariantRequest request) {
        return new VariantSpecification(
                request.stock(),
                request.price() != null ? request.price() : null,
                request.imageUrl(),
                request.options().stream()
                        .map(opt -> new VariantOption(opt.name(), opt.value()))
                        .collect(Collectors.toList())
        );
    }

    private List<ProductVariantSummaryResponse> mapVariants(Set<ProductVariant> variants) {
        return variants.stream()
                .map(variant -> new ProductVariantSummaryResponse(
                        variant.getId().toString(),
                        variant.getSku(),
                        variant.getVariantOptions().stream()
                                .map(opt -> new ProductVariantSummaryResponse.VariantOptionResponse(
                                        opt.getName(),
                                        opt.getValue()
                                ))
                                .collect(Collectors.toList()),
                        variant.getPrice(),
                        variant.getStock()
                ))
                .collect(Collectors.toList());
    }

    public ProductDetailResponse toDetailResponse(Product product) {
        return new ProductDetailResponse(
                product.getId().getId(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                product.getStock(),
                product.getPrice(),
                product.isActive(),
                mapVariants(product.getProductVariants())
        );
    }
}