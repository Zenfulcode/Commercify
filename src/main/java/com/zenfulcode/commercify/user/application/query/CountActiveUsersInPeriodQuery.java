package com.zenfulcode.commercify.user.application.query;

import com.zenfulcode.commercify.metrics.application.dto.MetricsQuery;

import java.time.LocalDate;

public record CountActiveUsersInPeriodQuery(
        String region,
        LocalDate startDate,
        LocalDate endDate
) {
    public static CountActiveUsersInPeriodQuery of(MetricsQuery metricsQuery) {
        return new CountActiveUsersInPeriodQuery(
                metricsQuery.region(),
                metricsQuery.startDate(),
                metricsQuery.endDate()
        );
    }
}
