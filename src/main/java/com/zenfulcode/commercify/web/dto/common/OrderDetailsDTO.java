package com.zenfulcode.commercify.web.dto.common;

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
}
