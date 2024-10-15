package com.gostavdev.commercify.orderservice.services;

import com.gostavdev.commercify.orderservice.dto.*;
import com.gostavdev.commercify.orderservice.api.CreateOrderRequest;
import com.gostavdev.commercify.orderservice.api.OrderLineRequest;
import com.gostavdev.commercify.orderservice.dto.mappers.OrderDTOMapper;
import com.gostavdev.commercify.orderservice.dto.mappers.OrderLineDTOMapper;
import com.gostavdev.commercify.orderservice.feignclients.PaymentsClient;
import com.gostavdev.commercify.orderservice.feignclients.ProductsClient;
import com.gostavdev.commercify.orderservice.model.Order;
import com.gostavdev.commercify.orderservice.model.OrderLine;
import com.gostavdev.commercify.orderservice.model.OrderStatus;
import com.gostavdev.commercify.orderservice.repositories.OrderLineRepository;
import com.gostavdev.commercify.orderservice.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(mapper);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(mapper);
    }

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request, String authHeader) {
        // 1. Validate the request
        validateCreateOrderRequest(request);

        // 2. Create the Order entity
        Order order = new Order(request.userId());

        // 3. Fetch and validate products, create OrderLines
        List<OrderLine> orderLines = createOrderLines(request, order, authHeader);

        // 4. Calculate order total
        double orderTotal = calculateOrderTotal(orderLines);

        // 5. Set order details
        order.setOrderLines(orderLines);
        order.setTotalAmount(orderTotal);

        // 6. Save the order and order lines
        Order savedOrder = orderRepository.save(order);
        orderLineRepository.saveAll(orderLines);

//        // 7. Initiate payment process (async)
//        // 8. Update inventory (async)
//        // 9. Send order confirmation (async)

        return mapper.apply(savedOrder);
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request.userId() == null || request.orderLines().isEmpty()) {
            throw new IllegalArgumentException("Invalid order request");
        }
    }

    private List<OrderLine> createOrderLines(CreateOrderRequest request, Order order, String authHeader) {
        return request.orderLines().stream()
                .collect(Collectors.groupingBy(OrderLineRequest::productId))
                .entrySet().stream()
                .map(entry -> {
                    ProductDto product = productsClient.getProductById(entry.getKey(), authHeader);
                    if (product == null) {
                        throw new RuntimeException("Product not found with ID: " + entry.getKey());
                    }
                    validateProductAvailability(product, entry.getValue());
                    return createOrderLine(product, entry.getValue(), order);
                })
                .collect(Collectors.toList());
    }

    private void validateProductAvailability(ProductDto product, List<OrderLineRequest> requests) {
        int totalQuantity = requests.stream().mapToInt(OrderLineRequest::quantity).sum();
        if (product.stock() < totalQuantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.productId());
        }
    }

    private OrderLine createOrderLine(ProductDto product, List<OrderLineRequest> requests, Order order) {
        OrderLine orderLine = new OrderLine();
        orderLine.setProductId(product.productId());
        orderLine.setProduct(product);
        orderLine.setQuantity(requests.stream().mapToInt(OrderLineRequest::quantity).sum());
        orderLine.setUnitPrice(product.unitPrice());
        orderLine.setOrder(order);
        orderLine.setStripeProductId(product.stripeId());
        return orderLine;
    }

    private double calculateOrderTotal(List<OrderLine> orderLines) {
        return orderLines.stream()
                .mapToDouble(line -> line.getUnitPrice() * line.getQuantity())
                .sum();
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
    public void deleteOrder(Long id) {
        paymentsClient.cancelPayment(id);

        orderLineRepository.deleteOrderLinesByOrder(orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found")));
        orderRepository.deleteById(id);
    }

    @Transactional
    public OrderDetails getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        List<OrderLineDTO> orderLines = getOrderLinesByOrderId(order);
        double totalPrice = calculateOrderTotal(order.getOrderLines());

        return new OrderDetails(mapper.apply(order), totalPrice, orderLines);
    }

    @Transactional(readOnly = true)
    public boolean isOrderOwnedByUser(Long orderId, Long userId) {
        System.out.println("orderId: " + orderId);
        System.out.println("userId: " + userId);

        return orderRepository.findById(orderId)
                .map(order -> order.getUserId().equals(userId))
                .orElse(false);
    }
}
