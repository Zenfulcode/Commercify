package com.zenfulcode.commercify.repository;

import com.zenfulcode.commercify.domain.model.OrderShippingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderShippingInfoRepository extends JpaRepository<OrderShippingInfo, Long> {
}