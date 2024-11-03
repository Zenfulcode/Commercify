package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderLineRequest;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.dto.*;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderLineMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final ProductService productService;
    private final OrderMapper mapper;
    private final OrderLineMapper olMapper;

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        validateCreateOrderRequest(request);

        OrderEntity order = OrderEntity.builder()
                .userId(request.userId())
                .status(OrderStatus.PENDING)
                .currency(request.currency())
                .build();

        List<OrderLineEntity> orderLines = createOrderLines(request, order);

        double orderTotal = calculateOrderTotal(orderLines);

        order.setOrderLines(orderLines);
        order.setTotalAmount(orderTotal);

        OrderEntity savedOrder = orderRepository.save(order);
        orderLineRepository.saveAll(orderLines);

        return mapper.apply(savedOrder);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<OrderLineDTO> getOrderLinesByOrderId(OrderEntity order) {
        return orderLineRepository.findByOrder(order).stream()
                .map(olMapper).toList();
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderLineRepository.deleteOrderLinesByOrder(orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found")));
        orderRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public OrderDetailsDTO getOrderById(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        List<OrderLineDTO> orderLines = getOrderLinesByOrderId(order);

        orderLines.forEach(ol -> ol.setProduct(productService.getProductById(ol.getProductId())));

        return new OrderDetailsDTO(mapper.apply(order), orderLines);
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request.userId() == null || request.orderLines().isEmpty() || request.currency() == null) {
            throw new IllegalArgumentException("Invalid order request");
        }
    }

    private List<OrderLineEntity> createOrderLines(CreateOrderRequest request, OrderEntity order) {
        return request.orderLines().stream()
                .collect(Collectors.groupingBy(line -> new AbstractMap.SimpleEntry<>(line.productId(), request.currency())))
                .entrySet().stream()
                .map(entry -> {
                    ProductDTO product = productService.getProductById(entry.getKey().getKey());
                    if (product == null) {
                        throw new RuntimeException("Product not found with ID: " + entry.getKey().getKey());
                    }

                    if (!product.getCurrency().equals(request.currency())) {
                        throw new RuntimeException("Price currency does not match order currency");
                    }

                    validateProductAvailability(product, entry.getValue());
                    return createOrderLine(product, entry.getValue(), order);
                })
                .collect(Collectors.toList());
    }

    private void validateProductAvailability(ProductDTO product, List<CreateOrderLineRequest> requests) {
        int totalQuantity = requests.stream().mapToInt(CreateOrderLineRequest::quantity).sum();
        if (product.getStock() < totalQuantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getId());
        }
    }

    private OrderLineEntity createOrderLine(ProductDTO product, List<CreateOrderLineRequest> requests, OrderEntity order) {
        OrderLineEntity orderLine = new OrderLineEntity();
        orderLine.setProductId(product.getId());
        orderLine.setProduct(product);
        orderLine.setQuantity(requests.stream().mapToInt(CreateOrderLineRequest::quantity).sum());
        orderLine.setUnitPrice(product.getUnitPrice());
        orderLine.setCurrency(product.getCurrency());
        orderLine.setOrder(order);
        return orderLine;
    }

    private double calculateOrderTotal(List<OrderLineEntity> orderLines) {
        return orderLines.stream()
                .mapToDouble(line -> line.getUnitPrice() * line.getQuantity())
                .sum();
    }

    @Transactional(readOnly = true)
    public boolean isOrderOwnedByUser(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUserId().equals(userId))
                .orElse(false);
    }

    public Page<OrderDTO> getAllOrders(PageRequest pageRequest) {
        return orderRepository.findAll(pageRequest)
                .map(mapper);
    }

    public Page<OrderDTO> getOrdersByUserId(Long userId, PageRequest pageRequest) {
        return orderRepository.findByUserId(userId, pageRequest)
                .map(mapper);
    }
}
