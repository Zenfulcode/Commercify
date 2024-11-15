package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.OrderStatus;
import com.zenfulcode.commercify.commercify.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);

    boolean existsByIdAndUserId(Long id, Long userId);

    @Query("SELECT DISTINCT o FROM OrderEntity o " +
            "JOIN FETCH o.orderLines ol " +
            "WHERE o.id = :orderId")
    Optional<OrderEntity> findByIdWithOrderLines(@PathVariable Long orderId);

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.userId = :userId " +
            "AND o.status IN :statuses")
    List<OrderEntity> findByUserIdAndStatusIn(
            @Param("userId") Long userId,
            @Param("statuses") Collection<OrderStatus> statuses
    );

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.status IN :statuses")
    List<OrderEntity> findByStatusIn(@Param("statuses") Collection<OrderStatus> statuses);

    @Query("SELECT COUNT(o) > 0 FROM OrderEntity o " +
            "WHERE o.status IN :statuses " +
            "AND o.userId = :userId")
    boolean hasOrdersInStatus(
            @Param("userId") Long userId,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}