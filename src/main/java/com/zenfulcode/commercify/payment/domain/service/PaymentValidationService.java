package com.zenfulcode.commercify.payment.domain.service;

import com.zenfulcode.commercify.order.domain.model.Order;
import com.zenfulcode.commercify.order.domain.model.OrderStatus;
import com.zenfulcode.commercify.order.domain.service.OrderStateFlow;
import com.zenfulcode.commercify.payment.domain.exception.InvalidPaymentStateException;
import com.zenfulcode.commercify.payment.domain.exception.PaymentValidationException;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.model.PaymentMethod;
import com.zenfulcode.commercify.payment.domain.model.PaymentProvider;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentStatus;
import com.zenfulcode.commercify.payment.domain.valueobject.refund.RefundRequest;
import com.zenfulcode.commercify.shared.domain.model.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentValidationService {
    private final PaymentStateFlow stateFlow;

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "EUR", "GBP", "DKK");
    private final PaymentProviderFactory paymentProviderFactory;
    private final OrderStateFlow orderStateFlow;

    /**
     * Validates payment creation
     */
    public void validateCreatePayment(Order order, PaymentMethod paymentMethod, PaymentProvider provider) {
        List<String> violations = new ArrayList<>();

        validateAmount(order.getTotalAmount(), violations);

        // Validate payment method
        validatePaymentMethod(paymentMethod, provider, violations);

        // Validate order state
        validateOrderForPayment(order, violations);

        if (!violations.isEmpty()) {
            throw new PaymentValidationException("Payment validation failed", violations);
        }
    }

    /**
     * Validates payment for capture
     */
    public void validatePaymentCapture(Payment payment, Money captureAmount) {
        List<String> violations = new ArrayList<>();

        // Validate payment state
        if (payment.getStatus() != PaymentStatus.RESERVED) {
            violations.add("Payment must be in RESERVED state to be captured");
        }

        // Validate capture amount matches payment amount
        if (!captureAmount.equals(payment.getAmount())) {
            violations.add("Capture amount must match payment amount");
        }

        if (!violations.isEmpty()) {
            throw new PaymentValidationException("Payment capture validation failed", violations);
        }
    }

    /**
     * Validates payment for refund
     */
    public void validateRefundRequest(Payment payment, RefundRequest refundRequest) {
        List<String> violations = new ArrayList<>();

        // Validate payment state
        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            violations.add("Only paid payments can be refunded");
        }

        if (refundRequest.reason() == null) {
            violations.add("Refund reason is required");
        }

        // Validate refund amount
        validateRefundAmount(payment, refundRequest.amount(), violations);

        if (!violations.isEmpty()) {
            throw new PaymentValidationException("Refund validation failed", violations);
        }
    }

    /**
     * Validates payment status transition
     */
    public void validateStatusTransition(Payment payment, PaymentStatus newStatus) {
        if (!stateFlow.canTransitionTo(payment.getStatus(), newStatus)) {
            throw new InvalidPaymentStateException(
                    payment.getId(),
                    payment.getStatus(),
                    newStatus,
                    "Invalid payment status transition"
            );
        }
    }

    private void validateAmount(Money amount, List<String> violations) {
        // Validate currency
        if (!SUPPORTED_CURRENCIES.contains(amount.getCurrency())) {
            violations.add("Unsupported currency: " + amount.getCurrency());
        }
    }

    private void validatePaymentMethod(PaymentMethod method, PaymentProvider provider, List<String> violations) {
        PaymentProviderService providerService = paymentProviderFactory.getProvider(provider);

        // Validate payment method is supported by provider
        if (!providerService.supportsPaymentMethod(method)) {
            violations.add("Payment method " + method + " is not supported by provider " + provider);
        }
    }

    private void validateOrderForPayment(Order order, List<String> violations) {
        // Validate order has total amount
        if (order.getTotalAmount() == null || order.getTotalAmount().isZero()) {
            violations.add("Order must have a valid total amount");
        }

        // Validate order status allows payment
        if (!orderStateFlow.canTransition(order.getStatus(), OrderStatus.PAID)) {
            violations.add("Order status does not allow payment");
        }
    }

    private void validateRefundAmount(Payment payment, Money refundAmount, List<String> violations) {
        // Validate currency matches
        if (!refundAmount.getCurrency().equals(payment.getAmount().getCurrency())) {
            violations.add("Refund currency must match payment currency");
        }

        // Validate refund amount does not exceed payment amount
        if (refundAmount.isGreaterThan(payment.getAmount())) {
            violations.add("Refund amount cannot exceed payment amount");
        }

        // Validate refund amount is positive
        if (refundAmount.isZero() || refundAmount.isNegative()) {
            violations.add("Refund amount must be positive");
        }
    }
}