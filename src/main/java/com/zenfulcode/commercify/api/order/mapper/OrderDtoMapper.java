package com.zenfulcode.commercify.api.order.mapper;

import com.zenfulcode.commercify.api.order.dto.request.CreateOrderLineRequest;
import com.zenfulcode.commercify.api.order.dto.request.CreateOrderRequest;
import com.zenfulcode.commercify.api.order.dto.response.*;
import com.zenfulcode.commercify.api.product.dto.response.PageInfo;
import com.zenfulcode.commercify.order.application.command.CreateOrderCommand;
import com.zenfulcode.commercify.order.application.dto.OrderDetailsDTO;
import com.zenfulcode.commercify.order.application.dto.OrderLineDTO;
import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.valueobject.Address;
import com.zenfulcode.commercify.order.domain.valueobject.CustomerDetails;
import com.zenfulcode.commercify.order.domain.valueobject.OrderLineDetails;
import com.zenfulcode.commercify.product.domain.valueobject.ProductId;
import com.zenfulcode.commercify.product.domain.valueobject.VariantId;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderDtoMapper {
    public CreateOrderCommand toCommand(CreateOrderRequest request) {
        CustomerDetails customerDetails = new CustomerDetails(
                request.customerDetails().firstName(),
                request.customerDetails().lastName(),
                request.customerDetails().email(),
                request.customerDetails().phone()
        );

        Address shippingAddress = new Address(
                request.shippingAddress().street(),
                request.shippingAddress().city(),
                request.shippingAddress().state(),
                request.shippingAddress().zipCode(),
                request.shippingAddress().country()
        );

        Address billingAddress = request.billingAddress() != null ? new Address(
                request.billingAddress().street(),
                request.billingAddress().city(),
                request.billingAddress().state(),
                request.billingAddress().zipCode(),
                request.billingAddress().country()
        ) : null;

        List<OrderLineDetails> orderLines = request.orderLines().stream()
                .map(this::toOrderLineDetails)
                .toList();

        return new CreateOrderCommand(
                UserId.of(request.userId()),
                request.currency(),
                customerDetails,
                shippingAddress,
                billingAddress,
                orderLines
        );
    }

    private OrderLineDetails toOrderLineDetails(CreateOrderLineRequest request) {
        return new OrderLineDetails(
                ProductId.of(request.productId()),
                VariantId.of(request.variantId()),
                request.quantity()
        );
    }

    public OrderDetailsResponse toResponse(OrderDetailsDTO dto) {
        return new OrderDetailsResponse(
                dto.id().toString(),
                dto.userId().toString(),
                dto.status().toString(),
                dto.currency(),
                new MoneyResponse(
                        dto.totalAmount().getAmount().doubleValue(),
                        dto.totalAmount().getCurrency()
                ),
                dto.orderLines().stream()
                        .map(this::toOrderLineResponse)
                        .collect(Collectors.toList()),
                toCustomerDetailsResponse(dto.customerDetails()),
                toAddressResponse(dto.shippingAddress()),
                dto.billingAddress() != null ?
                        toAddressResponse(dto.billingAddress()) : null,
                dto.createdAt()
        );
    }

    public PagedOrderResponse toPagedResponse(Page<Order> orderPage) {
        List<OrderSummaryResponse> orders = orderPage.getContent().stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());

        return new PagedOrderResponse(
                orders,
                new PageInfo(
                        orderPage.getNumber(),
                        orderPage.getSize(),
                        orderPage.getTotalElements(),
                        orderPage.getTotalPages()
                )
        );
    }

    private OrderSummaryResponse toSummaryResponse(Order order) {
        return new OrderSummaryResponse(
                order.getId().toString(),
                order.getUserId().toString(),
                order.getStatus().toString(),
                new MoneyResponse(
                        order.getTotalAmount().getAmount().doubleValue(),
                        order.getTotalAmount().getCurrency()
                ),
                order.getCreatedAt()
        );
    }

    private OrderLineResponse toOrderLineResponse(OrderLineDTO line) {
        return new OrderLineResponse(
                line.id().toString(),
                line.productId().toString(),
                line.variantId().toString(),
                line.quantity(),
                new MoneyResponse(
                        line.unitPrice().getAmount().doubleValue(),
                        line.unitPrice().getCurrency()
                ),
                new MoneyResponse(
                        line.total().getAmount().doubleValue(),
                        line.total().getCurrency()
                )
        );
    }

    private CustomerDetailsResponse toCustomerDetailsResponse(CustomerDetails details) {
        return new CustomerDetailsResponse(
                details.firstName(),
                details.lastName(),
                details.email(),
                details.phone()
        );
    }

    private AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.street(),
                address.city(),
                address.state(),
                address.zipCode(),
                address.country()
        );
    }
}
