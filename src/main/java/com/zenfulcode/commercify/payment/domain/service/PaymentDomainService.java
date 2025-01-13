package com.zenfulcode.commercify.payment.domain.service;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStatus;
import com.zenfulcode.commercify.payment.domain.valueobject.refund.RefundRequest;
import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentDomainService {
    private final PaymentValidationService validationService;
    private final PaymentStateFlow paymentStateFlow;
    private final DomainEventPublisher eventPublisher;

    /**
     * Creates a new payment for an order
     */
    public Payment createPayment(Order order, PaymentMethod paymentMethod, PaymentProvider provider) {
        validationService.validateCreatePayment(order, paymentMethod, provider);

        // Create payment
        Payment payment = Payment.create(
                order.getTotalAmount(),
                paymentMethod,
                provider
        );

        payment.setOrder(order);

        return payment;
    }

    /**
     * Processes a successful payment capture
     */
    public void capturePayment(Payment payment, String transactionId, Money capturedAmount) {
        validationService.validatePaymentCapture(payment, capturedAmount);

        payment.markAsCaptured(transactionId, capturedAmount);

        // Publish payment captured event
        eventPublisher.publish(payment.getDomainEvents());
    }

    /**
     * Handles payment failures
     */
    public void failPayment(Payment payment, String failureReason) {
        // Validate current state
        paymentStateFlow.validateStateTransition(payment.getStatus(), PaymentStatus.FAILED);

        payment.markAsFailed(failureReason);

        // Publish payment failed event
        eventPublisher.publish(payment.getDomainEvents());
    }

    /**
     * Processes payment refunds
     */
    public void refundPayment(Payment payment, RefundRequest refundRequest) {
        // Validate refund request
        validationService.validateRefundRequest(payment, refundRequest);

        // Process refund
        payment.processRefund(
                payment.getAmount(),
                refundRequest.reason(),
                refundRequest.notes()
        );

        // Publish refund event
        eventPublisher.publish(payment.getDomainEvents());
    }

    /**
     * Cancels a pending payment
     */
    public void cancelPayment(Payment payment) {
        // Validate cancellation
        validationService.validateStatusTransition(payment, PaymentStatus.CANCELLED);

        payment.cancel();

        // Publish payment cancelled event
        eventPublisher.publish(payment.getDomainEvents());
    }
}
