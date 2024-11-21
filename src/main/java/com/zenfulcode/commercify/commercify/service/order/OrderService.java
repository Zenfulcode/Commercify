package com.zenfulcode.commercify.commercify.service.order;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderLineRequest;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.dto.OrderLineDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.ProductVariantMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import com.zenfulcode.commercify.commercify.entity.ProductEntity;
import com.zenfulcode.commercify.commercify.entity.ProductVariantEntity;
import com.zenfulcode.commercify.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.commercify.repository.ProductVariantRepository;
import com.zenfulcode.commercify.commercify.service.StockManagementService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final ProductVariantRepository variantRepository;
    private final ProductVariantMapper productVariantMapper;

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // Validate request and check stock
        validationService.validateCreateOrderRequest(request);

        // Get and validate all products and variants upfront
        Map<Long, ProductEntity> products = getAndValidateProducts(request.orderLines());
        Map<Long, ProductVariantEntity> variants = getAndValidateVariants(request.orderLines());

        // Create order entity
        OrderEntity order = buildOrderEntity(userId, request, products, variants);
        OrderEntity savedOrder = orderRepository.save(order);
        // Update stock levels
//        stockService.updateStockLevels(order.getOrderLines());

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

    private Map<Long, ProductVariantEntity> getAndValidateVariants(List<CreateOrderLineRequest> orderLines) {
        Set<Long> variantIds = orderLines.stream()
                .map(CreateOrderLineRequest::variantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (variantIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, ProductVariantEntity> variants = variantRepository
                .findAllById(variantIds).stream().collect(Collectors.toMap(ProductVariantEntity::getId, Function.identity()));

        // Validate all requested variants exist and have sufficient stock
        orderLines.forEach(line -> {
            if (line.variantId() != null) {
                ProductVariantEntity variant = variants.get(line.variantId());
                if (variant == null) {
                    throw new ProductNotFoundException(line.variantId());
                }

                // TODO - Validate stock levels

                // Also validate that the variant belongs to the specified product
                if (!variant.getProduct().getId().equals(line.productId())) {
                    throw new IllegalArgumentException(
                            String.format("Variant %d does not belong to product %d",
                                    line.variantId(), line.productId())
                    );
                }
            }
        });

        return variants;
    }

    private OrderEntity buildOrderEntity(Long userId,
                                         CreateOrderRequest request,
                                         Map<Long, ProductEntity> products,
                                         Map<Long, ProductVariantEntity> variants) {
        // Create order lines first
        Set<OrderLineEntity> orderLines = request.orderLines().stream()
                .map(line -> createOrderLine(line.quantity(), products.get(line.productId()),
                        line.variantId() != null ? variants.get(line.variantId()) : null))
                .collect(Collectors.toSet());

        double totalAmount = calculationService.calculateTotalAmount(orderLines);

        OrderEntity order = OrderEntity.builder()
                .userId(userId)
                .orderLines(orderLines)
                .status(OrderStatus.PENDING)
                .currency(request.currency())
                .totalAmount(totalAmount)
                .build();

        // Set up bidirectional relationship
        orderLines.forEach(line -> line.setOrder(order));

        return order;
    }

    private OrderLineEntity createOrderLine(
            int quantity,
            ProductEntity product,
            ProductVariantEntity variant) {

        double unitPrice = variant != null && variant.getUnitPrice() != null ? variant.getUnitPrice() : product.getUnitPrice();

        return OrderLineEntity.builder()
                .productId(product.getId())
                .productVariant(variant)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .currency(product.getCurrency())
                .build();
    }

    private Map<Long, ProductEntity> getAndValidateProducts(List<CreateOrderLineRequest> orderLines) {
        Set<Long> productIds = orderLines.stream().map(CreateOrderLineRequest::productId).collect(Collectors.toSet());

        Map<Long, ProductEntity> products = productRepository.findAllById(productIds).stream().collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        // Validate all products exist and are active
        orderLines.forEach(line -> {
            ProductEntity product = products.get(line.productId());
            if (product == null) {
                throw new ProductNotFoundException(line.productId());
            }
            if (!product.getActive()) {
                throw new IllegalArgumentException("Product is not active: " + line.productId());
            }
        });

        return products;
    }

    private OrderEntity findOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private OrderDetailsDTO buildOrderDetailsDTO(OrderEntity order) {
        List<OrderLineDTO> orderLines = order.getOrderLines().stream()
                .map(line -> {
                    ProductEntity product = productRepository.findById(line.getProductId())
                            .orElseThrow(() -> new ProductNotFoundException(line.getProductId()));

                    return OrderLineDTO.builder()
                            .id(line.getId())
                            .productId(line.getProductId())
                            .quantity(line.getQuantity())
                            .unitPrice(line.getUnitPrice())
                            .currency(line.getCurrency())
                            .product(productMapper.apply(product))
                            .variant(line.getProductVariant() != null ?
                                    productVariantMapper.apply(line.getProductVariant()) : null)
                            .build();
                })
                .collect(Collectors.toList());

        return new OrderDetailsDTO(orderMapper.apply(order), orderLines);
    }
}
