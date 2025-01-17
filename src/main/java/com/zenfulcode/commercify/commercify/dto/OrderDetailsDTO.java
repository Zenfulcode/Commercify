package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class OrderDetailsDTO {
    private OrderDTO order;
    private List<OrderLineDTO> orderLines;
    private CustomerDetailsDTO customerDetails;
    private AddressDTO shippingAddress;
    private AddressDTO billingAddress;

    public OrderDetailsDTO() {

    }
}
