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
        PaymentStatus oldStatus = this.status;

        this.status = PaymentStatus.CAPTURED;
        this.transactionId = transactionId;
        this.completedAt = Instant.now();

        registerEvent(new PaymentStatusChangedEvent(
                this.id,
                order.getId(),
                oldStatus,
                PaymentStatus.CAPTURED,
                transactionId
        ));

        registerEvent(new PaymentCapturedEvent(
                id,
                order.getId(),
                capturedAmount,
                transactionId
        ));
    }

    public void markAsFailed(String reason) {
        PaymentStatus oldStatus = this.status;

        this.status = PaymentStatus.FAILED;
        this.errorMessage = reason;
        recordPaymentAttempt(false, reason);

        registerEvent(new PaymentStatusChangedEvent(
                this.id,
                order.getId(),
                oldStatus,
                PaymentStatus.FAILED,
                null
        ));

        registerEvent(new PaymentFailedEvent(
                this.id,
                order.getId(),
                reason
        ));
    }

    public void processRefund(Money refundAmount, RefundReason reason, String notes) {
        PaymentStatus oldStatus = this.status;

        // For full refunds
        if (refundAmount.equals(this.amount)) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }

        registerEvent(new PaymentStatusChangedEvent(
                this.id,
                order.getId(),
                oldStatus,
                this.status,
                null
        ));

        registerEvent(new IssuedRefundEvent(
                this.id,
                order.getId(),
                refundAmount,
                reason,
                notes,
                refundAmount.equals(this.amount)
        ));
    }

    public void cancel() {
        PaymentStatus oldStatus = this.status;

        this.status = PaymentStatus.CANCELLED;

        registerEvent(new PaymentStatusChangedEvent(
                this.id,
                order.getId(),
                oldStatus,
                PaymentStatus.CANCELLED,
                null
        ));

        registerEvent(new PaymentCancelledEvent(
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
        PaymentStatus oldStatus = this.status;

        this.status = PaymentStatus.RESERVED;

        registerEvent(new PaymentStatusChangedEvent(
                this.id,
                order.getId(),
                oldStatus,
                PaymentStatus.RESERVED,
                null
        ));

        registerEvent(new PaymentReservedEvent(
                this.id,
                order.getId(),
                transactionId
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
