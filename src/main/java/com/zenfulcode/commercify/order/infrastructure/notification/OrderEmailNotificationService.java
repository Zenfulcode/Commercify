package com.zenfulcode.commercify.order.infrastructure.notification;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderLine;
import com.zenfulcode.commercify.order.domain.service.OrderNotificationService;
import com.zenfulcode.commercify.shared.domain.exception.EmailSendingException;
import com.zenfulcode.commercify.shared.infrastructure.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEmailNotificationService implements OrderNotificationService {
    private final EmailService emailService;
    private final TemplateEngine templateEngine;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.order-dashboard}")
    private String orderDashboard;

    private static final String ORDER_CONFIRMATION_TEMPLATE = "order/confirmation-email";
    private static final String ORDER_STATUS_UPDATE_TEMPLATE = "order/status-update-email";
    private static final String ORDER_SHIPPING_TEMPLATE = "order/shipping-email";
    private static final String ADMIN_ORDER_TEMPLATE = "order/admin-order-notification";

    @Override
    public void sendOrderConfirmation(Order order) {
        try {
            Context context = createOrderContext(order);
            String emailContent = templateEngine.process(ORDER_CONFIRMATION_TEMPLATE, context);
            emailService.sendEmail(
                    order.getOrderShippingInfo().toCustomerDetails().email(),
                    "Order Confirmation - #" + order.getId(),
                    emailContent, true
            );
            log.info("Order confirmation email sent for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email", e);
            throw new EmailSendingException("Failed to send order confirmation email: " + e.getMessage());
        }
    }

    @Override
    public void sendOrderStatusUpdate(Order order) {
        try {
            Context context = createOrderContext(order);
            String emailContent = templateEngine.process(ORDER_STATUS_UPDATE_TEMPLATE, context);
            emailService.sendEmail(
                    order.getOrderShippingInfo().toCustomerDetails().email(),
                    "Order Status Update - #" + order.getId(),
                    emailContent, true
            );
            log.info("Order status update email sent for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to send order status update email", e);
            throw new EmailSendingException("Failed to send order status update email: " + e.getMessage());
        }
    }

    @Override
    public void sendShippingConfirmation(Order order) {
        try {
            Context context = createOrderContext(order);
            String emailContent = templateEngine.process(ORDER_SHIPPING_TEMPLATE, context);
            emailService.sendEmail(
                    order.getOrderShippingInfo().toCustomerDetails().email(),
                    "Order Shipped - #" + order.getId(),
                    emailContent, true
            );
            log.info("Order shipping confirmation email sent for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to send shipping confirmation email", e);
            throw new EmailSendingException("Failed to send shipping confirmation email: " + e.getMessage());
        }
    }

    @Override
    public void notifyAdminNewOrder(Order order) {
        try {
            Context context = createAdminOrderContext(order);
            String emailContent = templateEngine.process(ADMIN_ORDER_TEMPLATE, context);
            emailService.sendEmail(
                    adminEmail,
                    "New Order Received - #" + order.getId(),
                    emailContent, true
            );
            log.info("Admin notification sent for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to send admin notification", e);
            throw new EmailSendingException("Failed to send admin notification: " + e.getMessage());
        }
    }

    private Context createOrderContext(Order order) {
        Context context = new Context(Locale.getDefault());

        List<Map<String, Object>> orderItems = order.getOrderLines().stream()
                .map(this::createOrderItemMap)
                .toList();

        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("city", order.getOrderShippingInfo().toShippingAddress().city());
        shippingAddress.put("zipCode", order.getOrderShippingInfo().toShippingAddress().zipCode());
        shippingAddress.put("country", order.getOrderShippingInfo().toShippingAddress().country());
        shippingAddress.put("street", order.getOrderShippingInfo().toShippingAddress().street());
        shippingAddress.put("state", order.getOrderShippingInfo().toShippingAddress().state());
        context.setVariable("shippingAddress", shippingAddress);

        Map<String, Object> billingAddress = new HashMap<>();
        billingAddress.put("city", order.getOrderShippingInfo().toBillingAddress().city());
        billingAddress.put("zipCode", order.getOrderShippingInfo().toBillingAddress().zipCode());
        billingAddress.put("country", order.getOrderShippingInfo().toBillingAddress().country());
        billingAddress.put("street", order.getOrderShippingInfo().toBillingAddress().street());
        billingAddress.put("state", order.getOrderShippingInfo().toBillingAddress().state());
        context.setVariable("billingAddress", billingAddress);

        Map<String, Object> details = new HashMap<>();
        details.put("id", order.getId().toString());
        details.put("customerName", order.getOrderShippingInfo().getCustomerName());
        details.put("customerPhone", order.getOrderShippingInfo().getCustomerPhone());
        details.put("customerEmail", order.getOrderShippingInfo().getCustomerEmail());
        details.put("orderNumber", order.getId().toString());
        details.put("status", order.getStatus().toString());
        details.put("createdAt", order.getCreatedAt());
        details.put("currency", order.getCurrency());
        details.put("totalAmount", order.getTotalAmount().getAmount().doubleValue());
        details.put("items", orderItems);
        context.setVariable("order", details);
        return context;
    }

    private Context createAdminOrderContext(Order order) {
        Context context = createOrderContext(order);
        context.setVariable("adminOrderUrl", String.format("%s/%s", orderDashboard, order.getId().toString()));
        return context;
    }

    private Map<String, Object> createOrderItemMap(OrderLine orderLine) {
        Map<String, Object> items = new HashMap<>();

        items.put("name", orderLine.getProduct().getName());
        items.put("variant", orderLine.getProductVariant() != null ?
                orderLine.getProductVariant().getSku() : "");
        items.put("quantity", orderLine.getQuantity());
        items.put("sku", orderLine.getProductVariant() != null ?
                orderLine.getProductVariant().getSku() : orderLine.getProduct().getId().toString());
        items.put("unitPrice", orderLine.getUnitPrice().getAmount().doubleValue());
        items.put("total", orderLine.getTotal().getAmount().doubleValue());
        return items;
    }
}