package com.zenfulcode.commercify.commercify.service;

import com.zenfulcode.commercify.commercify.PaymentStatus;
import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import com.zenfulcode.commercify.commercify.repository.PaymentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentStatus getPaymentStatus(Long orderId) {
        Optional<PaymentEntity> payment = paymentRepository.findByOrderId(orderId);
        return payment.map(PaymentEntity::getStatus).orElse(PaymentStatus.NOT_FOUND);
    }
}