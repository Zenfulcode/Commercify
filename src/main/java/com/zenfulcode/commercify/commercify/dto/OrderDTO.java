package com.zenfulcode.commercify.commercify.dto;

import com.zenfulcode.commercify.commercify.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class OrderDTO {
    private long id;
    private Long userId;
    private String currency;
    private double totalAmount;
    private OrderStatus orderStatus;
    private int orderLinesAmount;
    private Instant createdAt;
    private Instant updatedAt;
}