package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.OrderShippingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderShippingInfoRepository extends JpaRepository<OrderShippingInfo, Integer> {
}