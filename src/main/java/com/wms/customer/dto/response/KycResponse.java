package com.wms.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycResponse {
    private String kycStatus;
    private String nik;
    private String pob;
    private String dob;
}

