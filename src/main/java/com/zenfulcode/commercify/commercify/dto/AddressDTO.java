package com.zenfulcode.commercify.commercify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean isBillingAddress;
    private boolean isShippingAddress;
}