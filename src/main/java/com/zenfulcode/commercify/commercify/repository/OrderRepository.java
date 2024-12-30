package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Integer> {
    Page<OrderEntity> findByUserId(Integer userId, Pageable pageable);

    boolean existsByIdAndUserId(Integer id, Integer userId);
}