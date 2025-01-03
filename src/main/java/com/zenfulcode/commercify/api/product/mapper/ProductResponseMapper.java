package com.zenfulcode.commercify.api.product.mapper;

import com.zenfulcode.commercify.api.product.dto.response.PageInfo;
import com.zenfulcode.commercify.api.product.dto.response.PagedProductResponse;
import com.zenfulcode.commercify.api.product.dto.response.ProductSummaryResponse;
import com.zenfulcode.commercify.api.product.dto.response.ProductVariantSummaryResponse;
import com.zenfulcode.commercify.product.domain.model.Product;
import com.zenfulcode.commercify.product.domain.model.ProductVariant;
import com.zenfulcode.commercify.product.domain.model.VariantOption;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class ProductResponseMapper {
    public PagedProductResponse toPagedResponse(Page<Product> productPage) {
        List<ProductSummaryResponse> items = productPage.getContent()
                .stream()
                .map(this::toSummaryResponse)
                .toList();

        PageInfo pageInfo = new PageInfo(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );

        return new PagedProductResponse(
                items,
                pageInfo
        );
    }

    private ProductSummaryResponse toSummaryResponse(Product product) {
        return new ProductSummaryResponse(
                product.getId().toString(),
                product.getName(),
                product.getDescription(),
                product.getImageUrl(),
                toPriceResponse(product),
                product.getStock()
        );
    }

    private ProductSummaryResponse.ProductPriceResponse toPriceResponse(Product product) {
        return new ProductSummaryResponse.ProductPriceResponse(
                product.getPrice().getAmount().doubleValue(),
                product.getPrice().getCurrency()
        );
    }

    private List<ProductVariantSummaryResponse> toVariantResponses(Set<ProductVariant> variants) {
        return variants.stream()
                .map(this::toVariantResponse)
                .toList();
    }

    private ProductVariantSummaryResponse toVariantResponse(ProductVariant variant) {
        return new ProductVariantSummaryResponse(
                variant.getId().toString(),
                variant.getSku(),
                toVariantOptionResponses(variant.getOptions()),
                toVariantPriceResponse(variant),
                variant.getStock() != null ? variant.getStock() : variant.getProduct().getStock()
        );
    }

    private List<ProductVariantSummaryResponse.VariantOptionResponse> toVariantOptionResponses(
            Set<VariantOption> options) {
        return options.stream()
                .map(option -> new ProductVariantSummaryResponse.VariantOptionResponse(
                        option.getName(),
                        option.getValue()
                ))
                .toList();
    }

    private ProductSummaryResponse.ProductPriceResponse toVariantPriceResponse(
            ProductVariant variant) {
        return new ProductSummaryResponse.ProductPriceResponse(
                variant.getPrice().getAmount().doubleValue(),
                variant.getPrice().getCurrency()
        );
    }
}