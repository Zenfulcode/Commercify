package com.zenfulcode.commercify.api.system;

import com.zenfulcode.commercify.api.system.dto.MetricsRequest;
import com.zenfulcode.commercify.api.system.dto.MetricsResponse;
import com.zenfulcode.commercify.metrics.application.MetricsApplicationService;
import com.zenfulcode.commercify.metrics.application.dto.MetricsQuery;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v2/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsApplicationService metricsService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MetricsResponse>> getMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer lastDays) {

        log.info("Getting metrics");

        MetricsRequest request = new MetricsRequest(startDate, endDate, lastDays);

        // Handle either date range or "last X days"
        if (lastDays != null) {
            request.setLastDays(lastDays);
        } else {
            request.setStartDate(startDate);
            request.setEndDate(endDate);
        }

        final MetricsQuery metricsQuery = MetricsQuery.of(request);

        MetricsResponse metrics = metricsService.getMetrics(metricsQuery);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MetricsResponse>> getMetricsWithFilters(
            @Validated @RequestBody MetricsRequest request) {

        final MetricsQuery metricsQuery = MetricsQuery.of(request);

        MetricsResponse metrics = metricsService.getMetrics(metricsQuery);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }
}
