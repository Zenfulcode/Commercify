package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.PaymentProvider;
import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.api.requests.PaymentRequest;
import com.zenfulcode.commercify.commercify.api.responses.CancelPaymentResponse;
import com.zenfulcode.commercify.commercify.api.responses.PaymentResponse;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    // Get payment status by orderId
    public PaymentStatus getPaymentStatus(Long orderId) {
        Optional<PaymentEntity> payment = paymentRepository.findByOrderId(orderId);
        return payment.map(PaymentEntity::getStatus).orElse(PaymentStatus.NOT_FOUND);
    }

    public CancelPaymentResponse cancelPayment(Long orderId) {
        PaymentEntity payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (payment == null) {
            return CancelPaymentResponse.PaymentNotFound();
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            return CancelPaymentResponse.PaymentAlreadyPaid();
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            return CancelPaymentResponse.PaymentAlreadyCanceled();
        }

        return CancelPaymentResponse.InvalidPaymentProvider();
    }

    public PaymentResponse makePayment(PaymentProvider provider, PaymentRequest paymentRequest) {
        return switch (provider) {
            case MOBILEPAY -> throw new UnsupportedOperationException("MobilePay is not supported yet");
            case STRIPE -> throw new UnsupportedOperationException("Stripe is not supported yet");
            default -> PaymentResponse.FailedPayment();
        };
    }
}