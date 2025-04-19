package com.zenfulcode.commercify.payment.infrastructure.persistence;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.repository.PaymentRepository;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPaymentRepository implements PaymentRepository {
    private final SpringDataJpaPaymentRepository repository;

    @Override
    public Payment save(Payment payment) {
        return repository.save(payment);
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Payment> findByProviderReference(String providerReference) {
        return repository.findPaymentByProviderReference(providerReference);
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionId) {
        return repository.findPaymentByTransactionId(transactionId);
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return repository.findPaymentByOrder_Id(orderId);
    }

    @Override
    public Page<Payment> findAll(PageRequest pageRequest) {
        return repository.findAll(pageRequest);
    }
}
