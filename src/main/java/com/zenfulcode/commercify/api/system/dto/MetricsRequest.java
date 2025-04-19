package com.zenfulcode.commercify.api.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class MetricsRequest {
    private LocalDate startDate;
    private LocalDate endDate;

    @Min(1)
    private Integer lastDays;

    public MetricsRequest(@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                          Integer lastDays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.lastDays = lastDays;
    }

    // Optional filters
    private String productCategory;
    private String region;

    @JsonIgnore
    public LocalDate getEffectiveStartDate() {
        if (lastDays != null) {
            return LocalDate.now().minusDays(lastDays);
        }
        return startDate != null ? startDate : LocalDate.now().minusDays(30); // Default 30 days
    }

    @JsonIgnore
    public LocalDate getEffectiveEndDate() {
        return endDate != null ? endDate : LocalDate.now();
    }
}
