package com.zenfulcode.commercify.repository;

import com.zenfulcode.commercify.domain.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}