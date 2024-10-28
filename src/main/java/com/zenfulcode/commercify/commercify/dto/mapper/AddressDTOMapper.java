package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.AddressDTO;
import com.zenfulcode.commercify.commercify.entity.AddressEntity;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AddressDTOMapper implements Function<AddressEntity, AddressDTO> {
    @Override
    public AddressDTO apply(AddressEntity address) {
        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isBillingAddress(address.isBillingAddress())
                .isShippingAddress(address.isShippingAddress())
                .build();
    }
}