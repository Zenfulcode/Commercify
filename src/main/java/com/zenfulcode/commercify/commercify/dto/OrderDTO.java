package com.zenfulcode.commercify.commercify.dto;

import com.zenfulcode.commercify.commercify.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private Long userId;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
