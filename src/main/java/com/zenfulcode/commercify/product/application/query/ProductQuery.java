package com.zenfulcode.commercify.product.application.query;


import com.zenfulcode.commercify.product.domain.valueobject.CategoryId;

public record ProductQuery(
        ProductQueryType type,
        CategoryId categoryId,
        int threshold
) {
    public static ProductQuery all() {
        return new ProductQuery(ProductQueryType.ALL, null, 0);
    }

    public static ProductQuery active() {
        return new ProductQuery(ProductQueryType.ACTIVE, null, 0);
    }

    public static ProductQuery byCategory(CategoryId categoryId) {
        return new ProductQuery(ProductQueryType.BY_CATEGORY, categoryId, 0);
    }

    public static ProductQuery lowStock(int threshold) {
        return new ProductQuery(ProductQueryType.LOW_STOCK, null, threshold);
    }
}
