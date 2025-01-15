package com.zenfulcode.commercify.commercify.service.email;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.service.UserManagementService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserManagementService userService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${admin.order-email}")
    private String orderEmailReceiver;

    @Async
    public void sendConfirmationEmail(String to, String token) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        Context context = new Context();
        context.setVariable("confirmationUrl",
                frontendUrl + "/confirm-email?token=" + token);

        String htmlContent = templateEngine.process("confirmation-email", context);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Confirm your email address");
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }

    @Async
    public void sendOrderConfirmation(OrderDetailsDTO orderDetails) throws MessagingException {
        OrderDTO order = orderDetails.getOrder();
        UserDTO user = userService.getUserById(order.getUserId());

        Context context = new Context();
        context.setVariable("order", createOrderContext(orderDetails));

        String template = "order-confirmation-email";
        String subject = String.format("Order Confirmation #%d - %s",
                order.getId(), order.getOrderStatus());

        sendTemplatedEmail(user.getEmail(), subject, template, context);
        log.info("Order confirmation sent to {}", user.getEmail());
    }

    @Async
    public void sendNewOrderNotification(OrderDetailsDTO orderDetails) throws MessagingException {
        OrderDTO order = orderDetails.getOrder();

        Context context = new Context();
        context.setVariable("order", createOrderContext(orderDetails));
        context.setVariable("dashboardUrl", frontendUrl + "/admin/orders/" + order.getId());

        String template = "new-order-notification-email";
        String subject = String.format("New Order Received - #%d", order.getId());

        sendTemplatedEmail(orderEmailReceiver, subject, template, context);
        log.info("New order notification sent to {}", orderEmailReceiver);
    }

    @Async
    public void sendOrderStatusUpdate(OrderDetailsDTO orderDetails) throws MessagingException {
        OrderDTO order = orderDetails.getOrder();
        UserDTO user = userService.getUserById(order.getUserId());

        Context context = new Context();
        context.setVariable("order", createOrderContext(orderDetails));

        String template = "order-confirmation-email";
        String subject = String.format("Order #%d Status Update - %s",
                order.getId(), order.getOrderStatus());

        sendTemplatedEmail(user.getEmail(), subject, template, context);
    }

    private void sendTemplatedEmail(String to, String subject, String template, Context context)
            throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        String htmlContent = templateEngine.process(template, context);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }

    private Map<String, Object> createOrderContext(OrderDetailsDTO orderDetails) {
        OrderDTO order = orderDetails.getOrder();
        Map<String, Object> orderContext = new HashMap<>();

        orderContext.put("id", order.getId());
        orderContext.put("status", order.getOrderStatus());
        orderContext.put("createdAt", order.getCreatedAt());
        orderContext.put("currency", order.getCurrency());
        orderContext.put("subTotal", order.getSubTotal());
        orderContext.put("totalPrice", order.getTotal());
        orderContext.put("shippingCost", order.getShippingCost());
        orderContext.put("customerName", orderDetails.getCustomerDetails().getFullName());
        orderContext.put("customerEmail", orderDetails.getCustomerDetails().getEmail());
        orderContext.put("customerPhone", orderDetails.getCustomerDetails().getPhone());

        // Add shipping and billing addresses
        orderContext.put("shippingAddress", orderDetails.getShippingAddress());
        orderContext.put("billingAddress", orderDetails.getBillingAddress());

        // Transform order lines into a format suitable for the template
        List<Map<String, Object>> items = orderDetails.getOrderLines().stream()
                .map(line -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", line.getProduct().getName());
                    item.put("quantity", line.getQuantity());
                    item.put("unitPrice", line.getUnitPrice());
                    item.put("total", line.getQuantity() * line.getUnitPrice());

                    if (line.getVariant() != null) {
                        String variantDetails = line.getVariant().getOptions().stream()
                                .map(opt -> opt.getName() + ": " + opt.getValue())
                                .collect(Collectors.joining(", "));
                        item.put("variant", variantDetails);
                    }

                    return item;
                })
                .collect(Collectors.toList());

        orderContext.put("items", items);

        if (order.getOrderStatus() == OrderStatus.SHIPPED) {
            // Add tracking URL if available
            orderContext.put("trackingUrl", frontendUrl + "/orders/" + order.getId() + "/tracking");
        }

        return orderContext;
    }
}
