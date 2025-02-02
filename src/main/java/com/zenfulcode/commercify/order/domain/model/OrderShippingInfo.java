package com.zenfulcode.commercify.order.domain.model;

import com.zenfulcode.commercify.order.domain.valueobject.Address;
import com.zenfulcode.commercify.order.domain.valueobject.CustomerDetails;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_shipping_info")
public class OrderShippingInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "customer_first_name")
    private String customerFirstName;

    @Column(name = "customer_last_name")
    private String customerLastName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    // Shipping address
    @Column(name = "shipping_street", nullable = false)
    private String shippingStreet;

    @Column(name = "shipping_city", nullable = false)
    private String shippingCity;

    @Column(name = "shipping_state")
    private String shippingState;

    @Column(name = "shipping_zip", nullable = false)
    private String shippingZip;

    @Column(name = "shipping_country", nullable = false)
    private String shippingCountry;

    // Billing address
    @Column(name = "billing_street")
    private String billingStreet;

    @Column(name = "billing_city")
    private String billingCity;

    @Column(name = "billing_state")
    private String billingState;

    @Column(name = "billing_zip")
    private String billingZip;

    @Column(name = "billing_country")
    private String billingCountry;

    public static OrderShippingInfo create(
            CustomerDetails customerDetails,
            Address shippingAddress,
            Address billingAddress
    ) {
        OrderShippingInfo info = new OrderShippingInfo();

        // Set customer details
        info.customerFirstName = customerDetails.firstName();
        info.customerLastName = customerDetails.lastName();
        info.customerEmail = customerDetails.email();
        info.customerPhone = customerDetails.phone();

        // Set shipping address
        info.shippingStreet = shippingAddress.street();
        info.shippingCity = shippingAddress.city();
        info.shippingState = shippingAddress.state();
        info.shippingZip = shippingAddress.zipCode();
        info.shippingCountry = shippingAddress.country();

        // Set billing address if provided
        if (billingAddress != null) {
            info.billingStreet = billingAddress.street();
            info.billingCity = billingAddress.city();
            info.billingState = billingAddress.state();
            info.billingZip = billingAddress.zipCode();
            info.billingCountry = billingAddress.country();
        }

        return info;
    }

    public CustomerDetails toCustomerDetails() {
        return new CustomerDetails(
                customerFirstName,
                customerLastName,
                customerEmail,
                customerPhone
        );
    }

    public Address toShippingAddress() {
        return new Address(
                shippingStreet,
                shippingCity,
                shippingState,
                shippingZip,
                shippingCountry
        );
    }

    public Address toBillingAddress() {
        if (!hasBillingAddress()) {
            return toShippingAddress();
        }
        return new Address(
                billingStreet,
                billingCity,
                billingState,
                billingZip,
                billingCountry
        );
    }

    public boolean hasBillingAddress() {
        return billingStreet != null && !billingStreet.isBlank();
    }
}