package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.CreateOrderLineRequest;
import com.zenfulcode.commercify.commercify.api.requests.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.dto.OrderLineDTO;
import com.zenfulcode.commercify.commercify.dto.ProductDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderDTOMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderLineDTOMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
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

    private final ProductService productService;

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(mapper);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(mapper);
    }

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        // 1. Validate the request
        validateCreateOrderRequest(request);

        // 2. Create the Order entity
        OrderEntity order = new OrderEntity(request.userId());

        // 3. Fetch and validate products, create OrderLines
        List<OrderLineEntity> orderLines = createOrderLines(request, order);

        // 4. Calculate order total
        double orderTotal = calculateOrderTotal(orderLines);

        // 5. Set order details
        order.setOrderLines(orderLines);
        order.setTotalAmount(orderTotal);

        // 6. Save the order and order lines
        OrderEntity savedOrder = orderRepository.save(order);
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

    private List<OrderLineEntity> createOrderLines(CreateOrderRequest request, OrderEntity order) {
        return request.orderLines().stream()
                .collect(Collectors.groupingBy(CreateOrderLineRequest::productId))
                .entrySet().stream()
                .map(entry -> {
                    ProductDTO product = productService.getProductById(entry.getKey());
                    if (product == null) {
                        throw new RuntimeException("Product not found with ID: " + entry.getKey());
                    }
                    validateProductAvailability(product, entry.getValue());
                    return createOrderLine(product, entry.getValue(), order);
                })
                .collect(Collectors.toList());
    }

    private void validateProductAvailability(ProductDTO product, List<CreateOrderLineRequest> requests) {
        int totalQuantity = requests.stream().mapToInt(CreateOrderLineRequest::quantity).sum();
        if (product.getStock() < totalQuantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getProductId());
        }
    }

    private OrderLineEntity createOrderLine(ProductDTO product, List<CreateOrderLineRequest> requests, OrderEntity order) {
        OrderLineEntity orderLine = new OrderLineEntity();
        orderLine.setProductId(product.getProductId());
        orderLine.setProduct(product);
        orderLine.setQuantity(requests.stream().mapToInt(CreateOrderLineRequest::quantity).sum());
        orderLine.setUnitPrice(product.getUnitPrice());
        orderLine.setOrder(order);
        orderLine.setStripeProductId(product.getStripeId());
        return orderLine;
    }

    private double calculateOrderTotal(List<OrderLineEntity> orderLines) {
        return orderLines.stream()
                .mapToDouble(line -> line.getUnitPrice() * line.getQuantity())
                .sum();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.updateStatus(status);
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
        double totalPrice = calculateOrderTotal(order.getOrderLines());

        orderLines.forEach(ol -> ol.setProduct(productService.getProductById(ol.getProductId())));

        return new OrderDetailsDTO(mapper.apply(order), totalPrice, orderLines);
    }

    @Transactional(readOnly = true)
    public boolean isOrderOwnedByUser(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUserId().equals(userId))
                .orElse(false);
    }
}
