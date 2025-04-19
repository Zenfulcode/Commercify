package com.zenfulcode.commercify.metrics.application.dto;

import com.zenfulcode.commercify.api.system.dto.MetricsRequest;

import java.time.LocalDate;

public record MetricsQuery(LocalDate startDate, LocalDate endDate, String productCategory,
                           String region) {

    public static MetricsQuery of(MetricsRequest metricsRequest) {
        return new MetricsQuery(
                metricsRequest.getEffectiveStartDate(),
                metricsRequest.getEffectiveEndDate(),
                metricsRequest.getProductCategory(),
                metricsRequest.getRegion()
        );
    }
}
