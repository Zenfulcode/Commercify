package com.zenfulcode.commercify.commercify.controller;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderLineRequest;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.api.responses.orders.CreateOrderResponse;
import com.zenfulcode.commercify.commercify.api.responses.orders.GetOrderResponse;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.dto.ProductVariantEntityDto;
import com.zenfulcode.commercify.commercify.service.OrderService;
import com.zenfulcode.commercify.commercify.service.ProductService;
import com.zenfulcode.commercify.commercify.viewmodel.OrderDetailsViewModel;
import com.zenfulcode.commercify.commercify.viewmodel.OrderViewModel;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final ProductService productService;
    private final PagedResourcesAssembler<OrderViewModel> pagedResourcesAssembler;

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
            "id", "userId", "status", "currency", "totalAmount", "createdAt", "updatedAt"
    );

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest orderRequest) {
        try {
            if (orderRequest.currency() == null || orderRequest.currency().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(CreateOrderResponse.from("Currency is required"));
            }

            // Validate that variants exist and belong to their respective products
            for (CreateOrderLineRequest line : orderRequest.orderLines()) {
                if (line.variantId() != null) {
                    ProductVariantEntityDto variant = productService.getProductVariant(line.productId(), line.variantId());
                    if (variant == null) {
                        return ResponseEntity.badRequest()
                                .body(CreateOrderResponse.from("Invalid variant ID: " + line.variantId()));
                    }
                    if (variant.getStock() < line.quantity()) {
                        return ResponseEntity.badRequest()
                                .body(CreateOrderResponse.from("Insufficient stock for variant: " + line.variantId()));
                    }
                }
            }

            OrderDTO orderDTO = orderService.createOrder(orderRequest);
            return ResponseEntity.ok(CreateOrderResponse.from(OrderViewModel.fromDTO(orderDTO)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(CreateOrderResponse.from("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(CreateOrderResponse.from(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            validateSortField(sortBy);
            Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<OrderViewModel> orders = orderService.getOrdersByUserId(userId, pageRequest).map(OrderViewModel::fromDTO);

            return ResponseEntity.ok(pagedResourcesAssembler.toModel(orders));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request parameters: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            validateSortField(sortBy);
            Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<OrderViewModel> orders = orderService.getAllOrders(pageRequest).map(OrderViewModel::fromDTO);

            return ResponseEntity.ok(pagedResourcesAssembler.toModel(orders));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request parameters: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER') and @orderService.isOrderOwnedByUser(#orderId, authentication.principal.id) or hasRole('ADMIN')")
    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrderById(@PathVariable Long orderId) {
        try {
            final OrderDetailsDTO orderDetails = orderService.getOrderById(orderId);
            final OrderDetailsViewModel orderDetailsViewModel = OrderDetailsViewModel.fromDTO(orderDetails);

            return ResponseEntity.ok(GetOrderResponse.from(orderDetailsViewModel));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(GetOrderResponse.from(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody String status
    ) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orderService.updateOrderStatus(id, orderStatus);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private void validateSortField(String sortBy) {
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }
    }
}