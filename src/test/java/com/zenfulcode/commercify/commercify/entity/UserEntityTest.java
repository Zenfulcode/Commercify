package com.zenfulcode.commercify.commercify.entity;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    private UserEntity user;

    @BeforeEach
    void setUp() {
        AddressEntity address = AddressEntity.builder()
                .street("123 Test St")
                .city("Test City")
                .state("Test State")
                .postalCode("12345")
                .country("Test Country")
                .isBillingAddress(true)
                .isShippingAddress(false)
                .build();

        user = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .roles(List.of("USER"))
                .addresses(new ArrayList<>(List.of(address)))
                .createdAt(new Date())
                .build();

        // Set up bidirectional relationship
        address.setUser(user);
    }

    @Test
    @DisplayName("Should create user with builder pattern")
    void testUserBuilder() {
        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals(1, user.getRoles().size());
        assertEquals("USER", user.getRoles().get(0));
    }

    @Test
    @DisplayName("Should implement UserDetails correctly")
    void testUserDetailsImplementation() {
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
        assertEquals("test@example.com", user.getUsername());
    }

    @Test
    @DisplayName("Should handle authorities correctly")
    void testAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.get(0).getAuthority());
    }

    @Test
    @DisplayName("Should handle empty roles")
    void testEmptyRoles() {
        user.setRoles(Collections.emptyList());
        assertTrue(user.getAuthorities().isEmpty());
    }

    @Nested
    @DisplayName("Address Management Tests")
    class AddressManagementTests {

        @Test
        @DisplayName("Should get billing address")
        void testGetBillingAddress() {
            AddressEntity billingAddress = user.getBillingAddress();
            assertNotNull(billingAddress);
            assertTrue(billingAddress.isBillingAddress());
            assertEquals("123 Test St", billingAddress.getStreet());
        }

        @Test
        @DisplayName("Should get shipping address")
        void testGetShippingAddress() {
            // Add a shipping address
            AddressEntity shippingAddress = AddressEntity.builder()
                    .street("456 Ship St")
                    .city("Ship City")
                    .state("Ship State")
                    .postalCode("67890")
                    .country("Ship Country")
                    .isShippingAddress(true)
                    .isBillingAddress(false)
                    .user(user)
                    .build();
            user.getAddresses().add(shippingAddress);

            AddressEntity retrievedAddress = user.getShippingAddress();
            assertNotNull(retrievedAddress);
            assertTrue(retrievedAddress.isShippingAddress());
            assertEquals("456 Ship St", retrievedAddress.getStreet());
        }

        @Test
        @DisplayName("Should handle no billing address")
        void testNoBillingAddress() {
            user.setAddresses(Collections.emptyList());
            assertNull(user.getBillingAddress());
        }
    }
}