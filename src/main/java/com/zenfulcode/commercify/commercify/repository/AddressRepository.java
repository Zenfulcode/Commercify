package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
}