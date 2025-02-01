package com.zenfulcode.commercify.payment.domain.service;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.payment.domain.event.PaymentCreatedEvent;
import com.zenfulcode.commercify.payment.domain.exception.PaymentNotFoundException;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.repository.PaymentRepository;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStatus;
import com.zenfulcode.commercify.payment.domain.valueobject.TransactionId;
import com.zenfulcode.commercify.payment.domain.valueobject.refund.RefundRequest;
import com.zenfulcode.commercify.shared.domain.event.DomainEventPublisher;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentDomainService {
    private final PaymentValidationService validationService;
    private final DomainEventPublisher eventPublisher;
    private final PaymentRepository paymentRepository;

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

        payment.registerEvent(new PaymentCreatedEvent(
                this,
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getProvider()
        ));

        return payment;
    }

    /**
     * Processes a successful payment capture
     */
    public void capturePayment(Payment payment, TransactionId transactionId, Money capturedAmount) {
        validationService.validatePaymentCapture(payment, capturedAmount);

        payment.markAsCaptured(transactionId, capturedAmount);

        // Publish payment captured event
        eventPublisher.publish(payment.getDomainEvents());
    }

    /**
     * Handles payment failures
     */
    public void failPayment(Payment payment, String failureReason) {
        failPayment(payment, failureReason, PaymentStatus.FAILED);
    }

    public void failPayment(Payment payment, String failureReason, PaymentStatus status) {
        // Validate current state
        validationService.validateStatusTransition(payment, status);

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

        eventPublisher.publish(payment.getDomainEvents());
    }

    /**
     * Get payment by ID
     */
    public Payment getPaymentById(PaymentId paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    public Payment getPaymentByProviderReference(String providerReference) {
        return paymentRepository.findByProviderReference(providerReference)
                .orElseThrow(() -> new PaymentNotFoundException(providerReference));
    }

    public void authorizePayment(Payment payment) {
        validationService.validateStatusTransition(payment, PaymentStatus.RESERVED);

        payment.reserve();

        eventPublisher.publish(payment.getDomainEvents());
    }
}
