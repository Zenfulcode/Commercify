package com.zenfulcode.commercify.order.application.query;

import com.zenfulcode.commercify.metrics.application.dto.MetricsQuery;

import java.time.LocalDate;

public record CountOrdersInPeriodQuery(
        String productCategory,
        String region,
        LocalDate startDate,
        LocalDate endDate
) {
    public static CountOrdersInPeriodQuery of(MetricsQuery metricsQuery) {
        return new CountOrdersInPeriodQuery(
                metricsQuery.productCategory(),
                metricsQuery.region(),
                metricsQuery.startDate(),
                metricsQuery.endDate()
        );
    }
}
