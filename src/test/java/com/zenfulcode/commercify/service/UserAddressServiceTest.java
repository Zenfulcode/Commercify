package com.zenfulcode.commercify.service;

import com.zenfulcode.commercify.web.dto.common.AddressDTO;
import com.zenfulcode.commercify.web.dto.common.UserDTO;
import com.zenfulcode.commercify.web.dto.mapper.AddressMapper;
import com.zenfulcode.commercify.web.dto.mapper.UserMapper;
import com.zenfulcode.commercify.domain.model.Address;
import com.zenfulcode.commercify.domain.model.User;
import com.zenfulcode.commercify.repository.AddressRepository;
import com.zenfulcode.commercify.repository.UserRepository;
import com.zenfulcode.commercify.service.core.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private UserManagementService userManagementService;

    private User user;
    private Address shippingAddress;
    private AddressDTO address;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        shippingAddress = Address.builder()
                .id(1L)
                .street("123 Ship St")
                .city("Ship City")
                .state("Ship State")
                .zipCode("12345")
                .country("Ship Country")
                .build();

        address = AddressDTO.builder()
                .street("789 Test St")
                .city("Test City")
                .state("Test State")
                .zipCode("54321")
                .country("Test Country")
                .build();

        userDTO = UserDTO.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Nested
    @DisplayName("Shipping Address Tests")
    class ShippingAddressTests {

        @Test
        @DisplayName("Should set shipping address successfully")
        void setShippingAddress_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userManagementService.setDefaultAddress(1L, address);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            Address savedAddress = userCaptor.getValue().getDefaultAddress();
            assertEquals(address.getStreet(), savedAddress.getStreet());
            assertEquals(address.getCity(), savedAddress.getCity());
            assertEquals(address.getState(), savedAddress.getState());
            assertEquals(address.getZipCode(), savedAddress.getZipCode());
            assertEquals(address.getCountry(), savedAddress.getCountry());
        }

        @Test
        @DisplayName("Should remove shipping address successfully")
        void removeShippingAddress_Success() {
            user.setDefaultAddress(shippingAddress);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.apply(any(User.class))).thenReturn(userDTO);

            userManagementService.removeDefaultAddress(1L);

            verify(userRepository).save(user);
            assertNull(user.getDefaultAddress());
        }

        @Test
        @DisplayName("Should throw exception when user not found - shipping")
        void setShippingAddress_UserNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> userManagementService.setDefaultAddress(1L, address));
        }
    }
}