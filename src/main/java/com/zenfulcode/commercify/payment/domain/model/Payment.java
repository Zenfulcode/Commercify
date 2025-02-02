package com.zenfulcode.commercify.payment.domain.model;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.payment.domain.event.*;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStatus;
import com.zenfulcode.commercify.payment.domain.valueobject.TransactionId;
import com.zenfulcode.commercify.payment.domain.valueobject.refund.RefundReason;
import com.zenfulcode.commercify.shared.domain.model.AggregateRoot;
import com.zenfulcode.commercify.shared.domain.model.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment extends AggregateRoot {

    @EmbeddedId
    private PaymentId id;

    @OneToOne(orphanRemoval = true)
    @JoinColumns({
            @JoinColumn(name = "order_id", referencedColumnName = "id")
    })
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", nullable = false)
    private PaymentProvider provider;

    @Column(name = "provider_reference")
    private String providerReference;

    @Embedded
    private TransactionId transactionId;

    @Column(name = "error_message")
    private String errorMessage;

    @ElementCollection
    @CollectionTable(
            name = "payment_attempts",
            joinColumns = @JoinColumn(name = "payment_id", referencedColumnName = "id")
    )
    private List<PaymentAttempt> paymentAttempts = new ArrayList<>();

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(name = "max_retries")
    private int maxRetries = 3;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    // Factory method for creating new payments
    public static Payment create(
            Money amount,
            PaymentMethod paymentMethod,
            PaymentProvider provider
    ) {
        Payment payment = new Payment();
        payment.id = PaymentId.generate();
        payment.amount = amount;
        payment.paymentMethod = paymentMethod;
        payment.provider = provider;
        payment.status = PaymentStatus.PENDING;

        return payment;
    }

    // Domain methods
    public void markAsCaptured(TransactionId transactionId, Money capturedAmount) {
        this.transactionId = transactionId;
        this.completedAt = Instant.now();

        updateStatus(PaymentStatus.CAPTURED);

        registerEvent(new PaymentCapturedEvent(
                this,
                id,
                order.getId(),
                capturedAmount,
                transactionId
        ));
    }

    public void markAsFailed(FailureReason reason, PaymentStatus status) {
        this.errorMessage = reason.getReason();
        recordPaymentAttempt(false, reason.getReason());

        updateStatus(status);

        registerEvent(new PaymentFailedEvent(
                this,
                this.id,
                order.getId(),
                reason,
                status
        ));
    }

    public void processRefund(Money refundAmount, RefundReason reason, String notes) {
        // For full refunds
        if (refundAmount.equals(this.amount)) {
            updateStatus(PaymentStatus.REFUNDED);
        } else {
            updateStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }

        registerEvent(new IssuedRefundEvent(
                this,
                this.id,
                order.getId(),
                refundAmount,
                reason,
                notes,
                refundAmount.equals(this.amount)
        ));
    }

    public void cancel() {
        updateStatus(PaymentStatus.CANCELLED);

        registerEvent(new PaymentCancelledEvent(
                this,
                this.id,
                order.getId(),
                "Payment cancelled"
        ));
    }

    public void updateProviderReference(String reference) {
        this.providerReference = reference;
    }

    public boolean canRetry() {
        return this.status == PaymentStatus.FAILED &&
                this.retryCount < this.maxRetries;
    }

    public void recordPaymentAttempt(boolean successful, String details) {
        PaymentAttempt attempt = new PaymentAttempt(
                Instant.now(),
                successful,
                details
        );

        this.paymentAttempts.add(attempt);
        if (!successful) {
            this.retryCount++;
        }
    }

    public void reserve() {
        updateStatus(PaymentStatus.RESERVED);

        registerEvent(new PaymentReservedEvent(
                this,
                this.id,
                order.getId(),
                transactionId
        ));
    }

    private void updateStatus(PaymentStatus newStatus) {
        PaymentStatus oldStatus = this.status;
        this.status = newStatus;

        registerEvent(new PaymentStatusChangedEvent(
                this,
                this.id,
                order.getId(),
                oldStatus,
                status,
                null
        ));
    }

    @Embeddable
    @Getter
    @NoArgsConstructor
    public static class PaymentAttempt {
        @Column(name = "attempt_time")
        private Instant attemptTime;

        @Column(name = "successful")
        private boolean successful;

        @Column(name = "details")
        private String details;

        public PaymentAttempt(Instant attemptTime, boolean successful, String details) {
            this.attemptTime = attemptTime;
            this.successful = successful;
            this.details = details;
        }
    }
}
