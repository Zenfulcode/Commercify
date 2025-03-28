package com.zenfulcode.commercify.user.infrastructure.persistence;

import com.zenfulcode.commercify.user.domain.model.User;
import com.zenfulcode.commercify.user.domain.model.UserStatus;
import com.zenfulcode.commercify.user.domain.valueobject.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
interface SpringDataJpaUserRepository extends JpaRepository<User, UserId> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query("""
                SELECT COUNT(u)
                FROM User u
                WHERE u.createdAt BETWEEN :startDate AND :endDate
            """)
    int findNewUsers(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}