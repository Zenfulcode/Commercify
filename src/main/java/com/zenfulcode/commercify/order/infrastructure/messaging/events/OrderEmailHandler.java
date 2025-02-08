package com.zenfulcode.commercify.order.infrastructure.messaging.events;

import com.zenfulcode.commercify.order.application.service.OrderApplicationService;
import com.zenfulcode.commercify.order.domain.event.OrderStatusChangedEvent;
import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.infrastructure.notification.OrderEmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEmailHandler {
    private final OrderApplicationService orderService;
    private final OrderEmailNotificationService notificationService;

    @Async
    @EventListener
    @Transactional(readOnly = true)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Sending email confirmation notification for order: {}", event.getOrderId());
        try {
            Order order = orderService.getOrderById(event.getOrderId());

            if (event.isPaidTransition()) {
                log.info("Sending order confirmation email for order: {}", order.getId());
                notificationService.sendOrderConfirmation(order);
                notificationService.notifyAdminNewOrder(order);

            } else if (event.isShippingTransition()) {
                log.info("Sending shipping confirmation email for order: {}", order.getId());
                notificationService.sendShippingConfirmation(order);
            } else if (event.isCompletedTransition()) {
                log.info("Sending order status update email for order: {}", order.getId());
                notificationService.sendOrderStatusUpdate(order);
            }
        } catch (Exception e) {
            log.error("Failed to send order status update notification", e);
        }
    }
}