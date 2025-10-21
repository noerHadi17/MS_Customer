package com.wms.customer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "mst_riskprofiles")
@Data
public class MstRiskProfileRef {
    @Id
    @Column(name = "risk_profile_id")
    private UUID riskProfileId;

    @Column(name = "profile_type")
    private String profileType;
}

