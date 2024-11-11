package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.addresses WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithAddresses(@Param("email") String email);
}
