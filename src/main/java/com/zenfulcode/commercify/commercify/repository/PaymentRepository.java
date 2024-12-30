package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {
    Optional<PaymentEntity> findByOrderId(Integer orderId);

    Optional<PaymentEntity> findByMobilePayReference(String mobilePayReference);
}