package com.zenfulcode.commercify.payment.domain.valueobject.refund;

import lombok.Getter;

/**
 * Enum for refund reasons
 */
@Getter
public enum RefundReason {
    CUSTOMER_REQUEST("Customer requested refund"),
    DUPLICATE_PAYMENT("Duplicate payment"),
    FRAUDULENT_CHARGE("Fraudulent charge"),
    ORDER_CANCELLED("Order cancelled"),
    PRODUCT_UNAVAILABLE("Product unavailable"),
    SHIPPING_ADDRESS_INVALID("Invalid shipping address"),
    PRODUCT_DAMAGED("Product damaged"),
    WRONG_ITEM("Wrong item delivered"),
    QUALITY_ISSUE("Product quality issue"),
    LATE_DELIVERY("Late delivery"),
    OTHER("Other reason");

    private final String description;

    RefundReason(String description) {
        this.description = description;
    }

}