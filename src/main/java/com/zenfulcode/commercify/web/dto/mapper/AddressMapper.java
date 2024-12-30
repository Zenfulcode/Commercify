package com.zenfulcode.commercify.web.dto.mapper;

import com.zenfulcode.commercify.web.dto.common.AddressDTO;
import com.zenfulcode.commercify.domain.model.Address;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AddressMapper implements Function<Address, AddressDTO> {
    @Override
    public AddressDTO apply(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .build();
    }
}