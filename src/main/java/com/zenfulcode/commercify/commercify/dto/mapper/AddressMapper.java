package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.AddressDTO;
import com.zenfulcode.commercify.commercify.entity.AddressEntity;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AddressMapper implements Function<AddressEntity, AddressDTO> {
    @Override
    public AddressDTO apply(AddressEntity address) {
        System.out.println("billing address: " + address.getIsBillingAddress());
        System.out.println("shipping address: " + address.getIsShippingAddress());

        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .isBilling(address.getIsBillingAddress())
                .isShipping(address.getIsShippingAddress())
                .build();
    }
}