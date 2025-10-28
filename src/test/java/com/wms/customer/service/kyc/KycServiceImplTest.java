package com.wms.customer.service.kyc;

import com.wms.customer.dto.request.KycRequest;
import com.wms.customer.dto.response.KycResponse;
import com.wms.customer.entity.MstCustomer;
import com.wms.customer.exception.BusinessException;
import com.wms.customer.kafka.AuditEventProducer;
import com.wms.customer.repository.MstCustomerRepository;
import com.wms.customer.service.implementation.KycServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KycServiceImpl Unit Tests")
class KycServiceImplTest {

    @Mock private MstCustomerRepository repository;
    @Mock private AuditEventProducer auditEventProducer;

    @InjectMocks private KycServiceImpl kycService;

    private MstCustomer customer;

    @BeforeEach
    void setUp() {
        customer = new MstCustomer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setEmail("ivan@mail.com");
        customer.setNik("1234567890123456");
        customer.setPob("Jakarta");
        customer.setDob(LocalDate.of(1999, 5, 12));
    }

    @Test
    @DisplayName("getStatus returns COMPLETE")
    void getStatus_ShouldReturnComplete_WhenAllKycDataPresent() {
        when(repository.findById(customer.getCustomerId())).thenReturn(Optional.of(customer));

        KycResponse res = kycService.getStatus(customer.getCustomerId());

        assertThat(res.getKycStatus()).isEqualTo("COMPLETE");
        assertThat(res.getNik()).isEqualTo("1234567890123456");
        assertThat(res.getPob()).isEqualTo("Jakarta");
    }

    @Test
    @DisplayName("getStatus returns INCOMPLETE")
    void getStatus_ShouldReturnIncomplete_WhenMissingFields() {
        customer.setPob("-");
        when(repository.findById(customer.getCustomerId())).thenReturn(Optional.of(customer));

        KycResponse res = kycService.getStatus(customer.getCustomerId());

        assertThat(res.getKycStatus()).isEqualTo("INCOMPLETE");
    }

    @Test
    @DisplayName("getStatus throws BusinessException if customer not found")
    void getStatus_ShouldThrow_WhenCustomerNotFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kycService.getStatus(UUID.randomUUID()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("AUTH_INVALID_CREDENTIALS");
    }

    @Test
    @DisplayName("submit success")
    void submit_ShouldUpdateCustomer_WhenValidNIK() {
        KycRequest req = new KycRequest("1234567890123456", "Bandung", LocalDate.of(2000, 1, 1));
        when(repository.findById(customer.getCustomerId())).thenReturn(Optional.of(customer));

        KycResponse res = kycService.submit(customer.getCustomerId(), req);

        assertThat(res.getKycStatus()).isEqualTo("COMPLETE");
        assertThat(res.getNik()).isEqualTo("1234567890123456");
        assertThat(res.getPob()).isEqualTo("Bandung");
        verify(repository).save(any(MstCustomer.class));
        verify(auditEventProducer).sendAuditEvent(
                eq("KYC_UPDATED"), anyString(), eq("ivan@mail.com"), eq("SUCCESS"), anyString());
    }

    @Test
    @DisplayName("submit invalid NIK (too short)")
    void submit_ShouldThrow_WhenNikTooShort() {
        KycRequest req = new KycRequest("12345", "Jakarta", LocalDate.now());

        assertThatThrownBy(() -> kycService.submit(customer.getCustomerId(), req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("KYC_VALIDATION_FAILED");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("submit invalid NIK (non-digit)")
    void submit_ShouldThrow_WhenNikHasNonDigit() {
        KycRequest req = new KycRequest("1234abcd5678efgh", "Jakarta", LocalDate.now());

        assertThatThrownBy(() -> kycService.submit(customer.getCustomerId(), req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("KYC_VALIDATION_FAILED");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("submit customer not found")
    void submit_ShouldThrow_WhenCustomerNotFound() {
        KycRequest req = new KycRequest("1234567890123456", "Jakarta", LocalDate.now());
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> kycService.submit(UUID.randomUUID(), req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("AUTH_INVALID_CREDENTIALS");
    }
}