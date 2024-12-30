package com.zenfulcode.commercify.service.core;

import com.zenfulcode.commercify.domain.enums.OrderStatus;
import com.zenfulcode.commercify.domain.model.*;
import com.zenfulcode.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.repository.OrderRepository;
import com.zenfulcode.commercify.repository.OrderShippingInfoRepository;
import com.zenfulcode.commercify.repository.ProductRepository;
import com.zenfulcode.commercify.repository.ProductVariantRepository;
import com.zenfulcode.commercify.service.StockManagementService;
import com.zenfulcode.commercify.service.validations.OrderValidationService;
import com.zenfulcode.commercify.web.dto.common.*;
import com.zenfulcode.commercify.web.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.web.dto.mapper.ProductMapper;
import com.zenfulcode.commercify.web.dto.mapper.ProductVariantMapper;
import com.zenfulcode.commercify.web.dto.request.order.CreateOrderLineRequest;
import com.zenfulcode.commercify.web.dto.request.order.CreateOrderRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final OrderValidationService validationService;
    private final StockManagementService stockService;
    private final ProductVariantRepository variantRepository;
    private final ProductVariantMapper productVariantMapper;
    private final OrderShippingInfoRepository orderShippingInfoRepository;

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request) {
        return createOrder(null, request);
    }

    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        // Validate request and check stock
        validationService.validateCreateOrderRequest(request);

        // Get and validate all products and variants upfront
        Map<Long, Product> products = getAndValidateProducts(request.orderLines());
        Map<Long, ProductVariant> variants = getAndValidateVariants(request.orderLines());

        // Get shipping information
        OrderShippingInfo shippingInfo = getShippingInformation(request);
        orderShippingInfoRepository.save(shippingInfo);

        // Create order entity
        Order order = buildOrderEntity(userId, request, products, variants, shippingInfo);
        Order savedOrder = orderRepository.save(order);

        return orderMapper.apply(savedOrder);
    }

    private OrderShippingInfo getShippingInformation(CreateOrderRequest request) {
        AddressDTO shippingAddress = request.shippingAddress();

        OrderShippingInfo.OrderShippingInfoBuilder shippingInfo = OrderShippingInfo.builder()
                .shippingStreet(shippingAddress.getStreet())
                .shippingCity(shippingAddress.getCity())
                .shippingState(shippingAddress.getState())
                .shippingZip(shippingAddress.getZipCode())
                .shippingCountry(shippingAddress.getCountry());

        AddressDTO billingAddress = request.billingAddress();
        if (billingAddress != null) {
            shippingInfo.billingStreet(billingAddress.getStreet())
                    .billingCity(billingAddress.getCity())
                    .billingState(billingAddress.getState())
                    .billingZip(billingAddress.getZipCode())
                    .billingCountry(billingAddress.getCountry());
        }

        shippingInfo.customerEmail(request.customerDetails().getEmail())
                .customerFirstName(request.customerDetails().getFirstName())
                .customerLastName(request.customerDetails().getLastName())
                .customerPhone(request.customerDetails().getPhone());

        return shippingInfo.build();
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = findOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();

        validationService.validateStatusTransition(oldStatus, newStatus);
        order.setStatus(newStatus);

        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = findOrderById(orderId);
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
        Order order = findOrderById(orderId);
        return buildOrderDetailsDTO(order);
    }

    @Transactional(readOnly = true)
    public boolean isOrderOwnedByUser(Long orderId, Long userId) {
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }

    private Map<Long, ProductVariant> getAndValidateVariants(List<CreateOrderLineRequest> orderLines) {
        Set<Long> variantIds = orderLines.stream()
                .map(CreateOrderLineRequest::variantId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (variantIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, ProductVariant> variants = variantRepository
                .findAllById(variantIds).stream().collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        // Validate all requested variants exist and have sufficient stock
        orderLines.forEach(line -> {
            if (line.variantId() != null) {
                ProductVariant variant = variants.get(line.variantId());
                if (variant == null) {
                    throw new ProductNotFoundException(line.variantId());
                }

                // TODO - Validate stock levels

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

    public double calculateTotalAmount(Set<OrderLine> orderLines) {
        return orderLines.stream()
                .mapToDouble(line -> line.getUnitPrice() * line.getQuantity())
                .sum();
    }

    private Order buildOrderEntity(Long userId,
                                   CreateOrderRequest request,
                                   Map<Long, Product> products,
                                   Map<Long, ProductVariant> variants,
                                   OrderShippingInfo shippingInfo) {
        // Create order lines first
        Set<OrderLine> orderLines = request.orderLines().stream()
                .map(line -> createOrderLine(line.quantity(), products.get(line.productId()),
                        line.variantId() != null ? variants.get(line.variantId()) : null))
                .collect(Collectors.toSet());

        double totalAmount = calculateTotalAmount(orderLines);

        Order order = Order.builder()
                .userId(userId)
                .orderLines(orderLines)
                .status(OrderStatus.PENDING)
                .currency(request.currency())
                .totalAmount(totalAmount)
                .orderShippingInfo(shippingInfo)
                .build();

        // Set up bidirectional relationship
        orderLines.forEach(line -> line.setOrder(order));

        return order;
    }

    private OrderLine createOrderLine(
            int quantity,
            Product product,
            ProductVariant variant) {

        double unitPrice = variant != null && variant.getUnitPrice() != null ? variant.getUnitPrice() : product.getUnitPrice();

        return OrderLine.builder()
                .productId(product.getId())
                .productVariant(variant)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .currency(product.getCurrency())
                .build();
    }

    private Map<Long, Product> getAndValidateProducts(List<CreateOrderLineRequest> orderLines) {
        Set<Long> productIds = orderLines.stream().map(CreateOrderLineRequest::productId).collect(Collectors.toSet());

        Map<Long, Product> products = productRepository.findAllById(productIds).stream().collect(Collectors.toMap(com.zenfulcode.commercify.domain.model.Product::getId, Function.identity()));

        orderLines.forEach(line -> {
            Product product = products.get(line.productId());
            if (product == null) {
                throw new ProductNotFoundException(line.productId());
            }
            if (!product.getActive()) {
                throw new IllegalArgumentException("Product is not active: " + line.productId());
            }
        });

        return products;
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private OrderDetailsDTO buildOrderDetailsDTO(Order order) {
        List<OrderLineDTO> orderLines = order.getOrderLines().stream()
                .map(line -> {
                    Product product = productRepository.findById(line.getProductId())
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

        OrderDTO orderDTO = orderMapper.apply(order);

        OrderShippingInfo shippingInfo = order.getOrderShippingInfo();

        System.out.println("shippingInfo = " + shippingInfo);

        CustomerDetailsDTO customerDetails = CustomerDetailsDTO.builder()
                .email(shippingInfo.getCustomerEmail())
                .firstName(shippingInfo.getCustomerFirstName())
                .lastName(shippingInfo.getCustomerLastName())
                .build();

        AddressDTO shippingAddress = AddressDTO.builder()
                .city(shippingInfo.getShippingCity())
                .country(shippingInfo.getShippingCountry())
                .state(shippingInfo.getShippingState())
                .street(shippingInfo.getShippingStreet())
                .zipCode(shippingInfo.getShippingZip())
                .build();

        AddressDTO billingAddress = AddressDTO.builder()
                .city(shippingInfo.getBillingCity())
                .country(shippingInfo.getBillingCountry())
                .state(shippingInfo.getBillingState())
                .street(shippingInfo.getBillingStreet())
                .zipCode(shippingInfo.getBillingZip())
                .build();

        return new OrderDetailsDTO(orderDTO, orderLines, customerDetails, shippingAddress, billingAddress);
    }
}
