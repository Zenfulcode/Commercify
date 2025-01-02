package com.zenfulcode.commercify.shared.domain.model;

import jakarta.persistence.Embeddable;
import lombok.Value;
import org.apache.commons.lang3.NotImplementedException;

import java.math.BigDecimal;

@Embeddable
@Value
public class Money {
    BigDecimal amount;
    String currency;

    public Money(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public Money(double amount, String currency) {
        this.amount = BigDecimal.valueOf(amount);
        this.currency = currency;
    }

    public Money() {
        this.amount = new BigDecimal(0);
        this.currency = "USD";
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public boolean isNegative() {
        throw new NotImplementedException("is negative has not been implemented");
    }

}