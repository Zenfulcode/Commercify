package com.zenfulcode.commercify.payment.domain.service;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.event.PaymentCreatedEvent;
import com.zenfulcode.commercify.payment.domain.exception.PaymentNotFoundException;
import com.zenfulcode.commercify.payment.domain.model.FailureReason;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.repository.PaymentRepository;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStatus;
import com.zenfulcode.commercify.payment.domain.valueobject.TransactionId;
import com.zenfulcode.commercify.payment.domain.valueobject.refund.RefundRequest;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentDomainService {
    private final PaymentValidationService validationService;
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

        paymentRepository.save(payment);
    }

    /**
     * Handles payment failures
     */
    public void failPayment(Payment payment, FailureReason failureReason, PaymentStatus status) {
        // Validate current state
        validationService.validateStatusTransition(payment, status);

        payment.markAsFailed(failureReason, status);

        paymentRepository.save(payment);
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

        paymentRepository.save(payment);
    }

    /**
     * Cancels a reserved payment
     */
    public void cancelPayment(Payment payment) {
        // Validate cancellation
        validationService.validateStatusTransition(payment, PaymentStatus.CANCELLED);

        payment.cancel();

        paymentRepository.save(payment);
    }

    public void authorizePayment(Payment payment) {
        validationService.validateStatusTransition(payment, PaymentStatus.RESERVED);

        payment.reserve();

        paymentRepository.save(payment);
    }

    public void updateProviderReference(Payment payment, String providerReference) {
        payment.updateProviderReference(providerReference);
        paymentRepository.save(payment);
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

    public Payment getPaymentByOrderId(OrderId orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));
    }
}
