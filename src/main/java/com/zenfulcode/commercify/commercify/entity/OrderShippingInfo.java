package com.zenfulcode.commercify.commercify.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "order_shipping_info")
@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderShippingInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "customer_first_name", nullable = false)
    private String customerFirstName;
    @Column(name = "customer_last_name", nullable = false)
    private String customerLastName;
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;
    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

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
}
