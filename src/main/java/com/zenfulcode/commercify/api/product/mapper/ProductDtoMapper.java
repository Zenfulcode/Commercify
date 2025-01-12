package com.zenfulcode.commercify.api.product.mapper;

import com.zenfulcode.commercify.api.product.dto.request.*;
import com.zenfulcode.commercify.api.product.dto.response.ProductDetailResponse;
import com.zenfulcode.commercify.api.product.dto.response.ProductSummaryResponse;
import com.zenfulcode.commercify.api.product.dto.response.ProductVariantSummaryResponse;
import com.zenfulcode.commercify.product.application.command.*;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.valueobject.*;
import com.zenfulcode.commercify.shared.domain.model.Money;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProductDtoMapper {

    public CreateProductCommand toCommand(CreateProductRequest request) {
        Money price = new Money(request.price().amount(), request.price().currency());

        return new CreateProductCommand(
                request.name(),
                request.description(),
                request.initialStock(),
                price,
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
                request.stock(),
                request.price() != null ?
                        new Money(request.price().amount(), request.price().currency()) : null,
                request.active()
        );
        return new UpdateProductCommand(productId, updateSpec);
    }

    public UpdateVariantPricesCommand toCommand(ProductId productId, UpdateVariantPricesRequest request) {
        List<VariantPriceUpdate> updates = request.updates().stream()
                .map(update -> new VariantPriceUpdate(
                        update.sku(),
                        new Money(update.price().amount(), update.price().currency())
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
                request.price() != null ?
                        new Money(request.price().amount(), request.price().currency()) : null,
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
                        toPriceResponse(variant.getPrice()),
                        variant.getStock()
                ))
                .collect(Collectors.toList());
    }

    private ProductSummaryResponse.ProductPriceResponse toPriceResponse(Money price) {
        return new ProductSummaryResponse.ProductPriceResponse(
                price.getAmount().doubleValue(), price.getCurrency()
        );
    }

    public ProductDetailResponse toDetailResponse(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getStock(),
                toPriceResponse(product.getPrice()),
                product.isActive(),
                mapVariants(product.getProductVariants())
        );
    }
}