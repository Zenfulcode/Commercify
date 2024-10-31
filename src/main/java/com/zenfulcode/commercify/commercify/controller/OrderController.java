package com.zenfulcode.commercify.commercify.controller;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.api.responses.CreateOrderResponse;
import com.zenfulcode.commercify.commercify.api.responses.GetOrderResponse;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.service.OrderService;
import com.zenfulcode.commercify.commercify.service.PaymentService;
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
    private final PaymentService paymentService;
    private final PagedResourcesAssembler<OrderDTO> pagedResourcesAssembler;

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
            "orderId", "userId", "status", "currency", "totalAmount", "createdAt", "updatedAt"
    );

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest orderRequest) {
        try {
            // Validate currency
            if (orderRequest.currency() == null || orderRequest.currency().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(CreateOrderResponse.from("Currency is required"));
            }

            OrderDTO orderDTO = orderService.createOrder(orderRequest);
            return ResponseEntity.ok(CreateOrderResponse.from(orderDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(CreateOrderResponse.from("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(CreateOrderResponse.from(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.userId")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String currency
    ) {
        try {
            validateSortField(sortBy);
            Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<OrderDTO> orders;
            if (currency != null && !currency.isBlank()) {
                orders = orderService.getOrdersByUserIdAndCurrency(userId, currency, pageRequest);
            } else {
                orders = orderService.getOrdersByUserId(userId, pageRequest);
            }

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
            @RequestParam(defaultValue = "orderId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String currency
    ) {
        try {
            validateSortField(sortBy);
            Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<OrderDTO> orders;
            if (currency != null && !currency.isBlank()) {
                orders = orderService.getAllOrdersByCurrency(currency, pageRequest);
            } else {
                orders = orderService.getAllOrders(pageRequest);
            }

            return ResponseEntity.ok(pagedResourcesAssembler.toModel(orders));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid request parameters: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('USER') and @orderService.isOrderOwnedByUser(#orderId, authentication.principal.id)")
    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrderById(@PathVariable Long orderId) {
        try {
            final OrderDetailsDTO orderDetails = orderService.getOrderById(orderId);
            return ResponseEntity.ok(GetOrderResponse.from(orderDetails));
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

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            paymentService.cancelPayment(id);
            orderService.deleteOrder(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private void validateSortField(String sortBy) {
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }
    }
}