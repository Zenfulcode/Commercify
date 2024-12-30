package com.zenfulcode.commercify.web.dto.mapper;

import com.zenfulcode.commercify.web.dto.common.UserDTO;
import com.zenfulcode.commercify.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserMapper implements Function<User, UserDTO> {
    private final AddressMapper addressDTOMapper;

    @Override
    public UserDTO apply(User user) {
        UserDTO.UserDTOBuilder userBuilder = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .createdAt(Date.from(user.getCreatedAt()));

        if (user.getDefaultAddress() != null) {
            userBuilder.defaultAddress(addressDTOMapper.apply(user.getDefaultAddress()));
        }

        return userBuilder.build();
    }
}
