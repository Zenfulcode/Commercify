package com.zenfulcode.commercify.order.application.query;

import com.zenfulcode.commercify.metrics.application.dto.MetricsQuery;

import java.time.LocalDate;

public record CalculateTotalRevenueQuery(
        String productCategory,
        String region,
        LocalDate startDate,
        LocalDate endDate
) {
    public static CalculateTotalRevenueQuery of(MetricsQuery metricsQuery) {
        return new CalculateTotalRevenueQuery(
                metricsQuery.productCategory(),
                metricsQuery.region(),
                metricsQuery.startDate(),
                metricsQuery.endDate()
        );
    }
}
