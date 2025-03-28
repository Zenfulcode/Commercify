package com.zenfulcode.commercify.product.application.query;

import com.zenfulcode.commercify.metrics.application.dto.MetricsQuery;

import java.time.LocalDate;

public record CountNewProductsInPeriodQuery(
        String productCategory,
        LocalDate startDate,
        LocalDate endDate
) {
    public static CountNewProductsInPeriodQuery of(MetricsQuery metricsQuery) {
        return new CountNewProductsInPeriodQuery(
                metricsQuery.productCategory(),
                metricsQuery.startDate(),
                metricsQuery.endDate()
        );
    }
}
