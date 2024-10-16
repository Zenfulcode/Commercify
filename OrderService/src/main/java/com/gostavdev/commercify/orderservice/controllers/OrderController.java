package com.gostavdev.commercify.orderservice.controllers;

import com.gostavdev.commercify.orderservice.api.CreateOrderResponse;
import com.gostavdev.commercify.orderservice.api.GetOrderResponse;
import com.gostavdev.commercify.orderservice.dto.OrderDTO;
import com.gostavdev.commercify.orderservice.dto.OrderDetails;
import com.gostavdev.commercify.orderservice.api.CreateOrderRequest;
import com.gostavdev.commercify.orderservice.model.OrderStatus;
import com.gostavdev.commercify.orderservice.services.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final PagedResourcesAssembler<OrderDTO> pagedResourcesAssembler;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest orderRequest, @RequestHeader("Authorization") String authHeader) {
        try {
            OrderDTO orderDTO = orderService.createOrder(orderRequest, authHeader);
            return ResponseEntity.ok(CreateOrderResponse.from(orderDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CreateOrderResponse.from(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.userId")
    @GetMapping("/user/{userId}")
    public ResponseEntity<PagedModel<EntityModel<OrderDTO>>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderDTO> orders = orderService.getOrdersByUserId(userId, pageRequest);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(orders));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderId") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<OrderDTO> orders = orderService.getAllOrders(pageRequest);
        return ResponseEntity.ok(pagedResourcesAssembler.toModel(orders));
    }

    @PreAuthorize("hasRole('USER') and @orderService.isOrderOwnedByUser(#orderId, authentication.principal.userId)")
    @GetMapping("/{orderId}")
    public ResponseEntity<GetOrderResponse> getOrderById(@PathVariable Long orderId, @RequestHeader("Authorization") String authHeader) {
        try {
            final OrderDetails orderDetails = orderService.getOrderById(orderId, authHeader);
            return ResponseEntity.ok(GetOrderResponse.from(orderDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(GetOrderResponse.from(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable Long id, @RequestBody String status) {
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        orderService.updateOrderStatus(id, orderStatus);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }
}
