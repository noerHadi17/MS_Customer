package com.wms.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = EntityNames.MST_CUSTOMER)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MstCustomer {
    @Id
    @Column(name = EntityNames.MstCustomer.CUSTOMER_ID)
    private UUID customerId;

    @Column(name = EntityNames.MstCustomer.NAME)
    private String name;

    @Column(name = EntityNames.MstCustomer.EMAIL, unique = true)
    private String email;

    @Column(name = EntityNames.MstCustomer.PASSWORD_HASH)
    private String passwordHash;

    @Column(name = EntityNames.MstCustomer.NIK)
    private String nik;

    @Column(name = EntityNames.MstCustomer.ADDRESS)
    private String address;

    @Column(name = EntityNames.MstCustomer.ID_RISK_PROFILE)
    private UUID idRiskProfile;

    @Column(name = EntityNames.MstCustomer.DOB)
    private LocalDate dob;

    @Column(name = EntityNames.MstCustomer.POB)
    private String pob;
}
