package com.zenfulcode.commercify.commercify.service.order;

import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
class OrderCalculationService {
    public double calculateTotalAmount(Set<OrderLineEntity> orderLines) {
        return orderLines.stream()
                .mapToDouble(line -> line.getUnitPrice() * line.getQuantity())
                .sum();
    }
}