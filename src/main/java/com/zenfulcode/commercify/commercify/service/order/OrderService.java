package com.zenfulcode.commercify.commercify.service.order;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderLineRequest;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.dto.OrderLineDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.commercify.service.StockManagementService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final OrderValidationService validationService;
    private final OrderCalculationService calculationService;
    private final StockManagementService stockService;
    private final OrderLineRepository orderLineRepository;

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // Validate request and check stock
        validationService.validateCreateOrderRequest(request);
        Map<Long, ProductEntity> products = getAndValidateProducts(request.orderLines());

        // Create order entity
        OrderEntity order = OrderEntity.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .currency(request.currency())
                .build();

        OrderEntity savedOrder = orderRepository.save(order);

        Set<OrderLineEntity> orderLines = createOrderLines(request.orderLines(), products, savedOrder);
        orderLineRepository.saveAll(orderLines);

        order.setOrderLines(orderLines);
        order.setTotalAmount(calculationService.calculateTotalAmount(orderLines));

        // Update stock levels
        stockService.updateStockLevels(order.getOrderLines());

        // Save and return
        return orderMapper.apply(savedOrder);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        OrderEntity order = findOrderById(orderId);
        validationService.validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        OrderEntity order = findOrderById(orderId);
        validationService.validateOrderCancellation(order);

        // Restore stock levels
        stockService.restoreStockLevels(order.getOrderLines());

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByUserId(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(orderMapper);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper);
    }

    @Transactional(readOnly = true)
    public OrderDetailsDTO getOrderById(Long orderId) {
        OrderEntity order = findOrderById(orderId);
        return buildOrderDetailsDTO(order);
    }

    @Transactional(readOnly = true)
    public boolean isOrderOwnedByUser(Long orderId, Long userId) {
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }

    private Set<OrderLineEntity> createOrderLines(List<CreateOrderLineRequest> lineRequests,
                                                  Map<Long, ProductEntity> products,
                                                  OrderEntity order) {
        return lineRequests.stream()
                .map(lineRequest -> {
                    ProductEntity product = products.get(lineRequest.productId());
                    return OrderLineEntity.builder()
                            .productId(product.getId())
                            .quantity(lineRequest.quantity())
                            .unitPrice(product.getUnitPrice())
                            .currency(product.getCurrency())
                            .order(order)
                            .build();
                })
                .collect(Collectors.toSet());
    }

    private Map<Long, ProductEntity> getAndValidateProducts(List<CreateOrderLineRequest> orderLines) {
        Set<Long> productIds = orderLines.stream()
                .map(CreateOrderLineRequest::productId)
                .collect(Collectors.toSet());

        Map<Long, ProductEntity> products = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        // Validate all products exist and are active
        orderLines.forEach(line -> {
            ProductEntity product = products.get(line.productId());
            if (product == null) {
                throw new ProductNotFoundException(line.productId());
            }
            if (!product.getActive()) {
                throw new IllegalArgumentException("Product is not active: " + line.productId());
            }
//            validationService.validateStockAvailability(product, line.quantity());
        });

        return products;
    }

    private OrderEntity findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private OrderDetailsDTO buildOrderDetailsDTO(OrderEntity order) {
        List<OrderLineDTO> orderLines = order.getOrderLines().stream()
                .map(line -> {
                    ProductEntity product = productRepository.findById(line.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(line.getProductId()));

                    OrderLineDTO lineDTO = OrderLineDTO.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .currency(line.getCurrency())
                            .build();

                    lineDTO.setProduct(productMapper.apply(product));
                    return lineDTO;
                })
                .collect(Collectors.toList());

        return new OrderDetailsDTO(orderMapper.apply(order), orderLines);
    }
}
