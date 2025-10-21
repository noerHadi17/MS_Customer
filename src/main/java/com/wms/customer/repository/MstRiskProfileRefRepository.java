package com.wms.customer.repository;

import com.wms.customer.entity.MstRiskProfileRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MstRiskProfileRefRepository extends JpaRepository<MstRiskProfileRef, UUID> {
}

