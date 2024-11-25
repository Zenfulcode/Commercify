package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.dto.AddressDTO;
import com.zenfulcode.commercify.commercify.dto.UserDTO;
import com.zenfulcode.commercify.commercify.dto.mapper.AddressMapper;
import com.zenfulcode.commercify.commercify.dto.mapper.UserMapper;
import com.zenfulcode.commercify.commercify.entity.AddressEntity;
import com.zenfulcode.commercify.commercify.entity.UserEntity;
import com.zenfulcode.commercify.commercify.repository.AddressRepository;
import com.zenfulcode.commercify.commercify.repository.UserRepository;
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

    private UserEntity user;
    private AddressEntity shippingAddress;
    private AddressEntity billingAddress;
    private AddressDTO address;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        shippingAddress = AddressEntity.builder()
                .id(1L)
                .street("123 Ship St")
                .city("Ship City")
                .state("Ship State")
                .zipCode("12345")
                .country("Ship Country")
                .build();

        billingAddress = AddressEntity.builder()
                .id(2L)
                .street("456 Bill St")
                .city("Bill City")
                .state("Bill State")
                .zipCode("67890")
                .country("Bill Country")
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
            when(userRepository.save(any(UserEntity.class))).thenReturn(user);

            userManagementService.setShippingAddress(1L, address);

            ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            AddressEntity savedAddress = userCaptor.getValue().getShippingAddress();
            assertEquals(address.getStreet(), savedAddress.getStreet());
            assertEquals(address.getCity(), savedAddress.getCity());
            assertEquals(address.getState(), savedAddress.getState());
            assertEquals(address.getZipCode(), savedAddress.getZipCode());
            assertEquals(address.getCountry(), savedAddress.getCountry());
        }

        @Test
        @DisplayName("Should remove shipping address successfully")
        void removeShippingAddress_Success() {
            user.setShippingAddress(shippingAddress);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(UserEntity.class))).thenReturn(user);
            when(userMapper.apply(any(UserEntity.class))).thenReturn(userDTO);

            userManagementService.removeShippingAddress(1L);

            verify(userRepository).save(user);
            assertNull(user.getShippingAddress());
        }

        @Test
        @DisplayName("Should throw exception when user not found - shipping")
        void setShippingAddress_UserNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> userManagementService.setShippingAddress(1L, address));
        }
    }

    @Nested
    @DisplayName("Billing Address Tests")
    class BillingAddressTests {

        @Test
        @DisplayName("Should set billing address successfully")
        void setBillingAddress_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(UserEntity.class))).thenReturn(user);

            userManagementService.setBillingAddress(1L, address);

            ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
            verify(userRepository).save(userCaptor.capture());

            AddressEntity savedAddress = userCaptor.getValue().getBillingAddress();
            assertEquals(address.getStreet(), savedAddress.getStreet());
            assertEquals(address.getCity(), savedAddress.getCity());
            assertEquals(address.getState(), savedAddress.getState());
            assertEquals(address.getZipCode(), savedAddress.getZipCode());
            assertEquals(address.getCountry(), savedAddress.getCountry());
        }

        @Test
        @DisplayName("Should remove billing address successfully")
        void removeBillingAddress_Success() {
            user.setBillingAddress(billingAddress);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(UserEntity.class))).thenReturn(user);
            when(userMapper.apply(any(UserEntity.class))).thenReturn(userDTO);

            userManagementService.removeBillingAddress(1L);

            verify(userRepository).save(user);
            assertNull(user.getBillingAddress());
        }

        @Test
        @DisplayName("Should throw exception when user not found - billing")
        void setBillingAddress_UserNotFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> userManagementService.setBillingAddress(1L, address));
        }
    }

    @Nested
    @DisplayName("Entity Relationship Tests")
    class EntityRelationshipTests {

        @Test
        @DisplayName("Should maintain bidirectional relationship - shipping")
        void testBidirectionalRelationship_Shipping() {
            user.setShippingAddress(shippingAddress);
            assertEquals(user, shippingAddress.getShippingUser());

            user.setShippingAddress(null);
            assertNull(shippingAddress.getShippingUser());
        }

        @Test
        @DisplayName("Should maintain bidirectional relationship - billing")
        void testBidirectionalRelationship_Billing() {
            user.setBillingAddress(billingAddress);
            assertEquals(user, billingAddress.getBillingUser());

            user.setBillingAddress(null);
            assertNull(billingAddress.getBillingUser());
        }

        @Test
        @DisplayName("Should handle shared address between shipping and billing")
        void testSharedAddress() {
            AddressEntity sharedAddress = AddressEntity.builder()
                    .id(3L)
                    .street("789 Shared St")
                    .city("Shared City")
                    .state("Shared State")
                    .zipCode("13579")
                    .country("Shared Country")
                    .build();

            user.setShippingAddress(sharedAddress);
            user.setBillingAddress(sharedAddress);

            assertEquals(sharedAddress, user.getShippingAddress());
            assertEquals(sharedAddress, user.getBillingAddress());
            assertEquals(user, sharedAddress.getShippingUser());
            assertEquals(user, sharedAddress.getBillingUser());
        }
    }
}