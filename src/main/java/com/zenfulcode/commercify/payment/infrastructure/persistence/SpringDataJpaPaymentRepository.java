package com.zenfulcode.commercify.payment.infrastructure.persistence;

import com.zenfulcode.commercify.order.domain.valueobject.OrderId;
import com.zenfulcode.commercify.payment.domain.model.Payment;
import com.zenfulcode.commercify.payment.domain.valueobject.PaymentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataJpaPaymentRepository extends JpaRepository<Payment, PaymentId> {
    Optional<Payment> findPaymentByProviderReference(String providerReference);

    Optional<Payment> findPaymentByTransactionId(String transactionId);

    Optional<Payment> findPaymentByOrder_Id(OrderId orderId);
}
