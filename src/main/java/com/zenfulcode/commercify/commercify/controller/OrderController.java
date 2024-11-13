package com.zenfulcode.commercify.commercify.controller;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.api.requests.orders.CreateOrderRequest;
import com.zenfulcode.commercify.commercify.api.requests.orders.OrderStatusUpdateRequest;
import com.zenfulcode.commercify.commercify.api.responses.ErrorResponse;
import com.zenfulcode.commercify.commercify.api.responses.orders.CreateOrderResponse;
import com.zenfulcode.commercify.commercify.api.responses.orders.GetOrderResponse;
import com.zenfulcode.commercify.commercify.dto.OrderDTO;
import com.zenfulcode.commercify.commercify.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.commercify.exception.InsufficientStockException;
import com.zenfulcode.commercify.commercify.exception.InvalidSortFieldException;
import com.zenfulcode.commercify.commercify.exception.OrderNotFoundException;
import com.zenfulcode.commercify.commercify.exception.ProductNotFoundException;
import com.zenfulcode.commercify.commercify.service.OrderService;
import com.zenfulcode.commercify.commercify.viewmodel.OrderDetailsViewModel;
import com.zenfulcode.commercify.commercify.viewmodel.OrderViewModel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final PagedResourcesAssembler<OrderViewModel> pagedResourcesAssembler;

    private static final Set<String> VALID_SORT_FIELDS = Set.of(
            "id", "userId", "status", "currency", "totalAmount", "createdAt", "updatedAt"
    );

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<?> createOrder(@Validated @RequestBody CreateOrderRequest orderRequest) {
        try {
            validateCreateOrderRequest(orderRequest);
            OrderDTO orderDTO = orderService.createOrder(orderRequest);
            return ResponseEntity.ok(CreateOrderResponse.from(OrderViewModel.fromDTO(orderDTO)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(CreateOrderResponse.from("Invalid request: " + e.getMessage()));
        } catch (ProductNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(CreateOrderResponse.from("Product not found: " + e.getMessage()));
        } catch (InsufficientStockException e) {
            return ResponseEntity.badRequest()
                    .body(CreateOrderResponse.from("Insufficient stock: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating order", e);
            return ResponseEntity.internalServerError()
                    .body(CreateOrderResponse.from("Error creating order: " + e.getMessage()));
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

            Page<OrderViewModel> orders = orderService.getOrdersByUserId(userId, pageRequest)
                    .map(OrderViewModel::fromDTO);

            return ResponseEntity.ok(pagedResourcesAssembler.toModel(orders));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters", e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request parameters: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving orders", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error retrieving orders: " + e.getMessage()));
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

            Page<OrderViewModel> orders = orderService.getAllOrders(pageRequest)
                    .map(OrderViewModel::fromDTO);

            return ResponseEntity.ok(pagedResourcesAssembler.toModel(orders));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters", e);
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request parameters: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving orders", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error retrieving orders: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('USER') and @orderService.isOrderOwnedByUser(#orderId, authentication.principal.id) or hasRole('ADMIN')")
    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDetailsDTO orderDetails = orderService.getOrderById(orderId);
            OrderDetailsViewModel orderDetailsViewModel = OrderDetailsViewModel.fromDTO(orderDetails);
            return ResponseEntity.ok(GetOrderResponse.from(orderDetailsViewModel));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving order", e);
            return ResponseEntity.badRequest()
                    .body(GetOrderResponse.from("Error retrieving order: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @Validated @RequestBody OrderStatusUpdateRequest request
    ) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(request.status().toUpperCase());
            orderService.updateOrderStatus(orderId, orderStatus);
            return ResponseEntity.ok().build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid order status: " + e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid status transition: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating order status", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error updating order status: " + e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok().build();
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Cannot cancel order: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error canceling order", e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Error canceling order: " + e.getMessage()));
        }
    }

    private void validateCreateOrderRequest(CreateOrderRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.userId() == null) {
            errors.add("User ID is required");
        }

        if (request.orderLines() == null || request.orderLines().isEmpty()) {
            errors.add("Order must contain at least one item");
        }

        if (request.currency() == null || request.currency().isBlank()) {
            errors.add("Currency is required");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }
    }

    private void validateSortField(String sortBy) {
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new InvalidSortFieldException("Invalid sort field: " + sortBy);
        }
    }
}