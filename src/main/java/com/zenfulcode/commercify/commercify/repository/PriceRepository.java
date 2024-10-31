package com.zenfulcode.commercify.commercify.repository;

import com.zenfulcode.commercify.commercify.entity.PriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<PriceEntity, Long> {
}