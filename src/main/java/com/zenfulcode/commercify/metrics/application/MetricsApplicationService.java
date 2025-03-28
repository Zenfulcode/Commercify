package com.zenfulcode.commercify.metrics.application;

import com.zenfulcode.commercify.api.system.dto.MetricsResponse;
import com.zenfulcode.commercify.metrics.application.dto.MetricsQuery;
import com.zenfulcode.commercify.order.application.query.CalculateTotalRevenueQuery;
import com.zenfulcode.commercify.order.application.query.CountOrdersInPeriodQuery;
import com.zenfulcode.commercify.order.application.service.OrderApplicationService;
import com.zenfulcode.commercify.product.application.query.CountNewProductsInPeriodQuery;
import com.zenfulcode.commercify.product.application.service.ProductApplicationService;
import com.zenfulcode.commercify.user.application.query.CountActiveUsersInPeriodQuery;
import com.zenfulcode.commercify.user.application.service.UserApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class MetricsApplicationService {
    private final OrderApplicationService orderApplicationService;
    private final ProductApplicationService productApplicationService;
    private final UserApplicationService userApplicationService;

    @Cacheable(value = "metricsCache", key = "{#metricsQuery.startDate, #metricsQuery.endDate, #metricsQuery.productCategory, #metricsQuery.region}")
    public MetricsResponse getMetrics(MetricsQuery metricsQuery) {
        LocalDate startDate = metricsQuery.startDate();
        LocalDate endDate = metricsQuery.endDate();

        log.info("Calculating metrics from {} to {}", startDate, endDate);

        final CalculateTotalRevenueQuery calculateTotalRevenueQuery = CalculateTotalRevenueQuery.of(metricsQuery);
        final CountOrdersInPeriodQuery countOrdersInPeriodQuery = CountOrdersInPeriodQuery.of(metricsQuery);
        final CountNewProductsInPeriodQuery countNewProductsInPeriodQuery = CountNewProductsInPeriodQuery.of(metricsQuery);
        final CountActiveUsersInPeriodQuery countActiveUsersInPeriodQuery = CountActiveUsersInPeriodQuery.of(metricsQuery);

        // Get metrics in parallel for better performance
        BigDecimal totalRevenue = orderApplicationService.calculateTotalRevenue(calculateTotalRevenueQuery);
        int totalOrders = orderApplicationService.countOrdersInPeriod(countOrdersInPeriodQuery);
        int newProductsAdded = productApplicationService.countNewProductsInPeriod(countNewProductsInPeriodQuery);
        int activeUsers = userApplicationService.countActiveUsersInPeriod(countActiveUsersInPeriodQuery);

        // Optional: calculate trends compared to previous period
        BigDecimal revenueChangePercent = calculateRevenueChangePercent(startDate, endDate, metricsQuery);

        return MetricsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .newProductsAdded(newProductsAdded)
                .activeUsers(activeUsers)
                .revenueChangePercent(revenueChangePercent)
                .build();
    }

    private BigDecimal calculateRevenueChangePercent(LocalDate startDate, LocalDate endDate, MetricsQuery metricsQuery) {
        // Calculate the same period length before the startDate
        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate previousPeriodStart = startDate.minusDays(periodDays);
        LocalDate previousPeriodEnd = startDate.minusDays(1);

        final CalculateTotalRevenueQuery currentRevenueQuery = new CalculateTotalRevenueQuery(
                metricsQuery.productCategory(),
                metricsQuery.region(),
                startDate,
                endDate
        );

        final CalculateTotalRevenueQuery previousRevenueQuery = new CalculateTotalRevenueQuery(
                metricsQuery.productCategory(),
                metricsQuery.region(),
                previousPeriodStart,
                previousPeriodEnd
        );

        BigDecimal currentRevenue = orderApplicationService.calculateTotalRevenue(currentRevenueQuery);
        BigDecimal previousRevenue = orderApplicationService.calculateTotalRevenue(previousRevenueQuery);

        if (previousRevenue.equals(BigDecimal.ZERO)) {
            return currentRevenue.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : new BigDecimal(100);
        }

        return currentRevenue.subtract(previousRevenue)
                .multiply(new BigDecimal(100)).divide(previousRevenue, 2, RoundingMode.HALF_UP);
    }
}
