package com.zenfulcode.commercify.commercify.dto;

import com.zenfulcode.commercify.commercify.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ActiveOrderDTO {
    private Long orderId;
    private OrderStatus status;
    private Integer quantity;
    private LocalDateTime createdAt;
}