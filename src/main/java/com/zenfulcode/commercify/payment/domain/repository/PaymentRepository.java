package com.zenfulcode.commercify.payment.domain.repository;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(PaymentId id);

    Optional<Payment> findByProviderReference(String providerReference);

    Optional<Payment> findByTransactionId(String transactionId);

    Optional<Payment> findByOrderId(OrderId orderId);

    Page<Payment> findAll(PageRequest pageRequest);
}
