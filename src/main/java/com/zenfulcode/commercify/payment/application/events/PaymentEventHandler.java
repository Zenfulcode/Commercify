package com.zenfulcode.commercify.payment.application.events;

import com.zenfulcode.commercify.order.application.command.UpdateOrderStatusCommand;
import com.zenfulcode.commercify.order.application.service.OrderApplicationService;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.payment.domain.event.PaymentCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventHandler {
    private final OrderApplicationService orderApplicationService;

    @EventListener
    @Transactional
    public void handlePaymentCancelled(PaymentCancelledEvent event) {
        log.info("Handling payment cancelled event for orderId: {}", event.getOrderId());

        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(
                event.getOrderId(),
                OrderStatus.CANCELLED
        );

        orderApplicationService.updateOrderStatus(command);
    }
}
