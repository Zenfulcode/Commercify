package com.zenfulcode.commercify.api.order;

import com.zenfulcode.commercify.api.order.dto.request.CreateOrderRequest;
import com.zenfulcode.commercify.api.order.dto.request.UpdateOrderStatusRequest;
import com.zenfulcode.commercify.api.order.dto.response.CreateOrderResponse;
import com.zenfulcode.commercify.api.order.dto.response.OrderDetailsResponse;
import com.zenfulcode.commercify.api.order.dto.response.PagedOrderResponse;
import com.zenfulcode.commercify.api.order.mapper.OrderDtoMapper;
import com.zenfulcode.commercify.order.application.command.CancelOrderCommand;
import com.zenfulcode.commercify.order.application.command.CreateOrderCommand;
import com.zenfulcode.commercify.order.application.command.UpdateOrderStatusCommand;
import com.zenfulcode.commercify.order.application.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.order.application.query.FindAllOrdersQuery;
import com.zenfulcode.commercify.order.application.query.FindOrdersByUserIdQuery;
import com.zenfulcode.commercify.order.application.service.OrderApplicationService;
import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.shared.interfaces.ApiResponse;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderApplicationService orderApplicationService;
    private final OrderDtoMapper orderDtoMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = orderDtoMapper.toCommand(request);
        OrderId orderId = orderApplicationService.createOrder(command);

        CreateOrderResponse response = new CreateOrderResponse(
                orderId.toString(),
                "Order created successfully"
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailsResponse>> getOrder(
            @PathVariable String orderId) {
        OrderDetailsDTO order = orderApplicationService.getOrderById(OrderId.of(orderId));
        OrderDetailsResponse response = orderDtoMapper.toResponse(order);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedOrderResponse>> getOrdersByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        FindOrdersByUserIdQuery query = new FindOrdersByUserIdQuery(
                UserId.of(userId),
                PageRequest.of(page, size)
        );

        Page<Order> orders = orderApplicationService.findOrdersByUserId(query);
        PagedOrderResponse response = orderDtoMapper.toPagedResponse(orders);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedOrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        FindAllOrdersQuery query = new FindAllOrdersQuery(PageRequest.of(page, size));
        Page<Order> orders = orderApplicationService.findAllOrders(query);
        PagedOrderResponse response = orderDtoMapper.toPagedResponse(orders);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody UpdateOrderStatusRequest request) {

        UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(
                OrderId.of(orderId),
                OrderStatus.valueOf(request.status())
        );

        orderApplicationService.updateOrderStatus(command);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully"));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> cancelOrder(@PathVariable String orderId) {
        CancelOrderCommand command = new CancelOrderCommand(OrderId.of(orderId));
        orderApplicationService.cancelOrder(command);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully"));
    }
}
