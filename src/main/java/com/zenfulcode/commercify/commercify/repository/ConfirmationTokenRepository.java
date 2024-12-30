package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.ConfirmationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationTokenEntity, Integer> {
    Optional<ConfirmationTokenEntity> findByToken(String token);
    Optional<ConfirmationTokenEntity> findByUserIdAndConfirmedFalse(Integer userId);
}