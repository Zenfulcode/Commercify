package com.zenfulcode.commercify.payment.infrastructure.message.events;

import com.zenfulcode.commercify.order.application.command.CancelOrderCommand;
import com.zenfulcode.commercify.order.application.command.UpdateOrderStatusCommand;
import com.zenfulcode.commercify.order.application.service.OrderApplicationService;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.payment.domain.event.PaymentCancelledEvent;
import com.zenfulcode.commercify.payment.domain.event.PaymentCapturedEvent;
import com.zenfulcode.commercify.payment.domain.event.PaymentFailedEvent;
import com.zenfulcode.commercify.payment.domain.event.PaymentReservedEvent;
import com.zenfulcode.commercify.payment.domain.model.FailureReason;
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

        CancelOrderCommand command = new CancelOrderCommand(event.getOrderId());

        orderApplicationService.cancelOrder(command);
    }

    @EventListener
    @Transactional
    public void handlePaymentCaptured(PaymentCapturedEvent event) {
        log.info("Handling payment captured event for orderId: {}", event.getOrderId());

        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(
                event.getOrderId(),
                OrderStatus.COMPLETED
        );

        orderApplicationService.updateOrderStatus(command);
    }

    @EventListener
    @Transactional
    public void handlePaymentReserved(PaymentReservedEvent event) {
        log.info("Handling payment reserved event for orderId: {}", event.getOrderId());

        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(
                event.getOrderId(),
                OrderStatus.PAID
        );

        orderApplicationService.updateOrderStatus(command);
    }

    @EventListener
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Handling payment failed event for orderId: {}", event.getOrderId());

        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(
                event.getOrderId(),
                mapFailureReasonToOrderStatus(event.getFailureReason())
        );

        orderApplicationService.updateOrderStatus(command);
    }

    private OrderStatus mapFailureReasonToOrderStatus(FailureReason failureReason) {
        return switch (failureReason) {
            case INSUFFICIENT_FUNDS, INVALID_PAYMENT_METHOD, PAYMENT_PROCESSING_ERROR, PAYMENT_METHOD_ERROR,
                 PAYMENT_PROVIDER_ERROR -> OrderStatus.FAILED;
            case PAYMENT_EXPIRED, PAYMENT_TERMINATED -> OrderStatus.ABANDONED;
            case UNKNOWN -> null;
        };
    }
}
