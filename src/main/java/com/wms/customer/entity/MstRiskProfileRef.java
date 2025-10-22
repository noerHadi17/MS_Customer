package com.wms.customer.entity;

import com.wms.customer.entity.EntityNames;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = EntityNames.MST_RISKPROFILES)
@Data
public class MstRiskProfileRef {
    @Id
    @Column(name = EntityNames.MstRiskprofiles.RISK_PROFILE_ID)
    private UUID riskProfileId;

    @Column(name = EntityNames.MstRiskprofiles.PROFILE_TYPE)
    private String profileType;
}


