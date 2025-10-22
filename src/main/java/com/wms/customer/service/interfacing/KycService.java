package com.wms.customer.service.interfacing;

import com.wms.customer.dto.request.KycRequest;
import com.wms.customer.dto.response.KycResponse;

import java.util.UUID;

public interface KycService {
    KycResponse getStatus(UUID customerId);
    KycResponse submit(UUID customerId, KycRequest req);
}

