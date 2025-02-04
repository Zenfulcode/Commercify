package com.zenfulcode.commercify.order.domain.service;

import com.zenfulcode.commercify.order.domain.model.Order;

public interface OrderNotificationService {
    void sendOrderConfirmation(Order order);

    void sendOrderStatusUpdate(Order order);

    void sendShippingConfirmation(Order order);

    void notifyAdminNewOrder(Order order);
}