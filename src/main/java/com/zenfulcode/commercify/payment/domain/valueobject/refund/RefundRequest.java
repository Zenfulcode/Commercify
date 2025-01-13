package com.zenfulcode.commercify.payment.domain.valueobject.refund;

import com.zenfulcode.commercify.payment.domain.exception.PaymentValidationException;
import com.zenfulcode.commercify.shared.domain.model.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class RefundRequest {
    @Column(name = "refund_amount")
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_reason")
    private RefundReason reason;

    @Column(name = "refund_notes")
    private String notes;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "requested_at")
    private Instant requestedAt;

    protected RefundRequest() {
        // For JPA
    }

    @Builder
    public RefundRequest(
            Money amount,
            RefundReason reason,
            String notes,
            String requestedBy
    ) {
        validate(amount, reason);

        this.amount = amount;
        this.reason = reason;
        this.notes = notes;
        this.requestedBy = requestedBy;
        this.requestedAt = Instant.now();
    }

    private void validate(Money amount, RefundReason reason) {
        List<String> violations = new ArrayList<>();

        if (amount == null) {
            violations.add("Refund amount is required");
        } else if (amount.isNegative() || amount.isZero()) {
            violations.add("Refund amount must be positive");
        }

        if (reason == null) {
            violations.add("Refund reason is required");
        }

        if (!violations.isEmpty()) {
            throw new PaymentValidationException("Invalid refund request", violations);
        }
    }

    public Money amount() {
        return amount;
    }

    public RefundReason reason() {
        return reason;
    }

    public String notes() {
        return notes;
    }

    public String requestedBy() {
        return requestedBy;
    }

    public Instant requestedAt() {
        return requestedAt;
    }

    public boolean isFullRefund(Money paymentAmount) {
        return amount.equals(paymentAmount);
    }
}
