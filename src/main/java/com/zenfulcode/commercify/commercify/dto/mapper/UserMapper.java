package com.zenfulcode.commercify.commercify.dto.mapper;

import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserMapper implements Function<UserEntity, UserDTO> {
    private final AddressMapper addressDTOMapper;

    @Override
    public UserDTO apply(UserEntity user) {
        UserDTO.UserDTOBuilder userBuilder = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .createdAt(Date.from(user.getCreatedAt()));

        if (user.getShippingAddress() != null) {
            userBuilder.shippingAddress(addressDTOMapper.apply(user.getShippingAddress()));
        }

        if (user.getBillingAddress() != null) {
            userBuilder.billingAddress(addressDTOMapper.apply(user.getBillingAddress()));
        }

        return userBuilder.build();
    }
}
