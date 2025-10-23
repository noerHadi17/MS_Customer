package com.wms.customer.repository;

import com.wms.customer.entity.MstRiskProfileRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for the risk profile reference master data.
 */
public interface MstRiskProfileRefRepository extends JpaRepository<MstRiskProfileRef, UUID> {
}
