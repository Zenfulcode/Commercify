package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.CreateOrderLineRequest;
import com.zenfulcode.commercify.commercify.api.requests.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.dto.*;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.OrderLineMapper;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import com.zenfulcode.commercify.commercify.entity.OrderLineEntity;
import com.zenfulcode.commercify.commercify.entity.PriceEntity;
import com.zenfulcode.commercify.commercify.repository.OrderLineRepository;
import com.zenfulcode.commercify.commercify.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
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
        // 1. Validate the request
        validateCreateOrderRequest(request);

        // 2. Create the Order entity
        OrderEntity order = new OrderEntity(request.userId(), request.currency());

        // 3. Fetch and validate products and prices, create OrderLines
        List<OrderLineEntity> orderLines = createOrderLines(request, order);

        // 4. Calculate order total
        double orderTotal = calculateOrderTotal(orderLines);

        // 5. Set order details
        order.setOrderLines(orderLines);
        order.setTotalAmount(orderTotal);

        // 6. Save the order and order lines
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
        double totalPrice = calculateOrderTotal(order.getOrderLines());

        orderLines.forEach(ol -> ol.setProduct(productService.getProductById(ol.getProductId())));

        return new OrderDetailsDTO(mapper.apply(order), totalPrice, orderLines);
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

                    PriceEntity price = priceDTOToEntity(product.getPrices().stream()
                            .filter(p -> p.getCurrency().equals(entry.getKey().getValue()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Price not found for product")));

                    if (!price.getCurrency().equals(request.currency())) {
                        throw new RuntimeException("Price currency does not match order currency");
                    }

                    validateProductAvailability(product, entry.getValue());
                    return createOrderLine(product, price, entry.getValue(), order);
                })
                .collect(Collectors.toList());
    }

    private void validateProductAvailability(ProductDTO product, List<CreateOrderLineRequest> requests) {
        int totalQuantity = requests.stream().mapToInt(CreateOrderLineRequest::quantity).sum();
        if (product.getStock() < totalQuantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getId());
        }
    }

    private OrderLineEntity createOrderLine(ProductDTO product, PriceEntity price, List<CreateOrderLineRequest> requests, OrderEntity order) {
        OrderLineEntity orderLine = new OrderLineEntity();
        orderLine.setProductId(product.getId());
        orderLine.setPriceId(price.getId());
        orderLine.setProduct(product);
        orderLine.setQuantity(requests.stream().mapToInt(CreateOrderLineRequest::quantity).sum());
        orderLine.setUnitPrice(price.getAmount());
        orderLine.setCurrency(price.getCurrency());
        orderLine.setStripePriceId(price.getStripePriceId());
        orderLine.setOrder(order);
        return orderLine;
    }

    private double calculateOrderTotal(List<OrderLineEntity> orderLines) {
        return orderLines.stream()
                .mapToDouble(line -> line.getUnitPrice() * line.getQuantity())
                .sum();
    }

    // Helper method to convert PriceDTO to PriceEntity
    private PriceEntity priceDTOToEntity(PriceDTO priceDTO) {
        return PriceEntity.builder()
                .id(priceDTO.getId())
                .currency(priceDTO.getCurrency())
                .amount(priceDTO.getAmount())
                .stripePriceId(priceDTO.getStripePriceId())
                .isDefault(priceDTO.getIsDefault())
                .active(priceDTO.getActive())
                .build();
    }


    @Transactional(readOnly = true)
    public boolean isOrderOwnedByUser(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUserId().equals(userId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getOrdersByUserIdAndCurrency(Long userId, String currency, Pageable pageable) {
        return orderRepository.findByUserIdAndCurrency(userId, currency, pageable)
                .map(mapper);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrdersByCurrency(String currency, Pageable pageable) {
        return orderRepository.findByCurrency(currency, pageable)
                .map(mapper);
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
