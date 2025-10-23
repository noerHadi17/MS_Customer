package com.wms.customer.repository;

import com.wms.customer.entity.MstCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing and managing customer records.
 */
public interface MstCustomerRepository extends JpaRepository<MstCustomer, UUID> {
    Optional<MstCustomer> findByEmail(String email);
    boolean existsByEmail(String email);
}
