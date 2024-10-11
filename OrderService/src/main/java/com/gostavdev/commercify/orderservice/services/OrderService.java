package com.gostavdev.commercify.orderservice.services;

import com.gostavdev.commercify.orderservice.dto.*;
import com.gostavdev.commercify.orderservice.dto.api.CreateOrderRequest;
import com.gostavdev.commercify.orderservice.dto.api.OrderLineRequest;
import com.gostavdev.commercify.orderservice.dto.mappers.OrderDTOMapper;
import com.gostavdev.commercify.orderservice.dto.mappers.OrderLineDTOMapper;
import com.gostavdev.commercify.orderservice.feignclients.PaymentsClient;
import com.gostavdev.commercify.orderservice.feignclients.ProductsClient;
import com.gostavdev.commercify.orderservice.feignclients.UserClient;
import com.gostavdev.commercify.orderservice.model.Order;
import com.gostavdev.commercify.orderservice.model.OrderLine;
import com.gostavdev.commercify.orderservice.model.OrderStatus;
import com.gostavdev.commercify.orderservice.repositories.OrderLineRepository;
import com.gostavdev.commercify.orderservice.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final OrderDTOMapper mapper;
    private final OrderLineDTOMapper olMapper;

    private final ProductsClient productsClient;
    private final PaymentsClient paymentsClient;
    private final UserClient userClient;

    @Transactional
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(mapper).toList();
    }

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        Order order = new Order(request.userId());

        List<OrderLine> orderLines = request.orderLines().stream()
                .collect(Collectors.groupingBy(OrderLineRequest::productId))
                .entrySet().stream()
                .map(entry -> {
                    ProductDto product = productsClient.getProductById(entry.getKey());

                    if (product == null) {
                        throw new RuntimeException("Product not found with ID: " + entry.getKey());
                    }

                    // Create OrderLine entity
                    OrderLine orderLine = new OrderLine();
                    orderLine.setProductId(entry.getKey());
                    orderLine.setProduct(product);
                    orderLine.setQuantity(entry.getValue().stream().mapToInt(OrderLineRequest::quantity).sum());
                    orderLine.setUnitPrice(product.unitPrice());
                    orderLine.setOrder(order);
                    orderLine.setStripeProductId(product.stripeId());

                    return orderLine;
                }).collect(Collectors.toList());

        // Create and save Order entity
        order.setOrderLines(orderLines);

        orderRepository.save(order);
        orderLineRepository.saveAll(orderLines);

        return mapper.apply(order);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.updateStatus(status);
        orderRepository.save(order);
    }

    @Transactional
    public List<OrderLineDTO> getOrderLinesByOrderId(Order order) {
        return orderLineRepository.findByOrder(order).stream()
                .map(olMapper).toList();
    }

    @Transactional
    public OrderDTO getOrderById(Long orderId) {
        return orderRepository.findById(orderId).map(mapper)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    @Transactional
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(mapper).collect(Collectors.toList());
    }

    @Transactional
    public void deleteOrder(Long id) {
        paymentsClient.cancelPayment(id);

        orderLineRepository.deleteOrderLinesByOrder(orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found")));
        orderRepository.deleteById(id);
    }

    @Transactional
    public OrderDetails getOrderDetailsById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));


        List<OrderLineDTO> orderLines = getOrderLinesByOrderId(order);

        return new OrderDetails(mapper.apply(order), orderLines);
    }
}
