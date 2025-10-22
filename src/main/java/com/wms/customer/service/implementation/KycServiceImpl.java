package com.wms.customer.service.implementation;

import com.wms.customer.dto.request.KycRequest;
import com.wms.customer.dto.response.KycResponse;
import com.wms.customer.entity.MstCustomer;
import com.wms.customer.exception.BusinessException;
import com.wms.customer.kafka.AuditEventProducer;
import com.wms.customer.repository.MstCustomerRepository;
import com.wms.customer.service.interfacing.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {
    private final MstCustomerRepository repository;
    private final AuditEventProducer auditEventProducer;

    @Override
    public KycResponse getStatus(UUID customerId) {
        MstCustomer c = repository.findById(customerId).orElseThrow(() -> new BusinessException("AUTH_INVALID_CREDENTIALS"));
        boolean complete = c.getNik() != null && !"-".equals(c.getNik())
                && c.getPob() != null && !"-".equals(c.getPob())
                && c.getDob() != null;
        String status = complete ? "COMPLETE" : "INCOMPLETE";
        return KycResponse.builder()
                .kycStatus(status)
                .nik(c.getNik())
                .pob(c.getPob())
                .dob(c.getDob() != null ? c.getDob().toString() : null)
                .build();
    }

    @Override
    public KycResponse submit(UUID customerId, KycRequest req) {
        if (req.getNik() == null || req.getNik().length() != 16 || !req.getNik().chars().allMatch(Character::isDigit)) {
            throw new BusinessException("KYC_VALIDATION_FAILED");
        }
        MstCustomer c = repository.findById(customerId).orElseThrow(() -> new BusinessException("AUTH_INVALID_CREDENTIALS"));
        c.setNik(req.getNik());
        c.setPob(req.getPob());
        c.setDob(req.getDob());
        repository.save(c);

        auditEventProducer.sendAuditEvent("KYC_UPDATED", c.getCustomerId().toString(), c.getEmail(), "SUCCESS", "KYC updated");
        return KycResponse.builder()
                .kycStatus("COMPLETE")
                .nik(c.getNik())
                .pob(c.getPob())
                .dob(c.getDob() != null ? c.getDob().toString() : null)
                .build();
    }
}
