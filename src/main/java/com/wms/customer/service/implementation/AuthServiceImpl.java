package com.wms.customer.service.implementation;

import com.wms.customer.dto.request.ChangePasswordRequest;
import com.wms.customer.dto.request.LoginRequest;
import com.wms.customer.dto.request.RegisterRequest;
import com.wms.customer.dto.response.LoginResponse;
import com.wms.customer.dto.response.RegisterResponse;
import com.wms.customer.entity.MstCustomer;
import com.wms.customer.exception.BusinessException;
import com.wms.customer.repository.MstCustomerRepository;
import com.wms.customer.config.CustomerDefaultsProperties;
import com.wms.customer.kafka.AuditEventProducer;
import com.wms.customer.repository.MstRiskProfileRefRepository;
import com.wms.customer.security.UserAuthJWT;
import com.wms.customer.security.UserAuthJWTUtility;
import com.wms.customer.service.interfacing.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Concrete authentication service handling registration, login, and credential updates for customers.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MstCustomerRepository customerRepository;
    private final UserAuthJWTUtility userAuthJWTUtility;
    private final UserAuthJWT userAuthJWT;
    private final AuditEventProducer auditEventProducer;
    private final CustomerDefaultsProperties defaults;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final MstRiskProfileRefRepository riskProfileRepository;

    public boolean checkEmail(String email) { return customerRepository.existsByEmail(email); }

    public RegisterResponse register(RegisterRequest req) {
        if (customerRepository.existsByEmail(req.getEmail())) {
            auditEventProducer.sendAuditEvent("REGISTER_FAILURE", null, req.getEmail(), "FAILURE", "Email already exists");
            throw new BusinessException("EMAIL_ALREADY_EXISTS");
        }
        try {
            MstCustomer c = new MstCustomer();
            c.setCustomerId(UUID.randomUUID());
            c.setName(req.getName());
            c.setEmail(req.getEmail());
            c.setPasswordHash(encoder.encode(req.getPassword()));
            c.setNik(defaults.getNikPlaceholder());
            c.setAddress(req.getAddress());
            c.setDob(defaults.isDefaultDobNow() ? java.time.LocalDate.now() : null);
            c.setPob(defaults.getPobPlaceholder());
            customerRepository.save(c);
            auditEventProducer.sendAuditEvent("REGISTER_SUCCESS", String.valueOf(c.getCustomerId()), c.getEmail(), "SUCCESS", "Registered");
            return RegisterResponse.builder().customerId(c.getCustomerId()).name(c.getName()).email(c.getEmail()).address(c.getAddress()).build();
        } catch (RuntimeException ex) {
            auditEventProducer.sendAuditEvent("REGISTER_FAILURE", null, req.getEmail(), "FAILURE", ex.getMessage());
            org.slf4j.LoggerFactory.getLogger(getClass()).error("Register failed for email={}", req.getEmail(), ex);
            throw new BusinessException("REGISTER_FAILURE");
        }
    }

    public LoginResponse login(LoginRequest req) {
        Optional<MstCustomer> opt = customerRepository.findByEmail(req.getEmail());
        if (opt.isEmpty() || !encoder.matches(req.getPassword(), opt.get().getPasswordHash())) {
            auditEventProducer.sendAuditEvent("LOGIN_FAILURE", null, req.getEmail(), "FAILURE", "Invalid credentials");
            throw new BusinessException("AUTH_INVALID_CREDENTIALS");
        }

        MstCustomer c = opt.get();

        // TTL token (ambil dari properti jika ada, fallback 360 menit)
        int ttlMinutes = (defaults != null && defaults.getJwtTtlMinutes() != null)
                ? defaults.getJwtTtlMinutes()
                : 360;

        // Status KYC lengkap atau belum
        boolean kycComplete = c.getNik() != null && !"-".equals(c.getNik())
                && c.getPob() != null && !c.getPob().isBlank();

        // Status CRP (Customer Risk Profile) lengkap atau belum
        boolean crpComplete = c.getIdRiskProfile() != null;

        // Ambil tipe risk profile (kalau ada)
        String riskProfileType = null;
        if (crpComplete) {
            try {
                riskProfileType = riskProfileRepository.findById(c.getIdRiskProfile())
                        .map(r -> r.getProfileType())
                        .orElse(null);
            } catch (Exception ignored) {
                // Hindari error jika tidak ada record
            }
        }

        // Generate JWT terenkripsi (pakai UserAuthJWT)
        String token = userAuthJWT.generateAuthToken(
                c.getCustomerId(),
                c.getName(),
                c.getEmail(),
                ttlMinutes,
                "wms-customer-service"
        );

        // Audit sukses login
        auditEventProducer.sendAuditEvent(
                "LOGIN_SUCCESS",
                String.valueOf(c.getCustomerId()),
                c.getEmail(),
                "SUCCESS",
                "Login"
        );

        // Build response
        return LoginResponse.builder()
                .customerId(c.getCustomerId())
                .name(c.getName())
                .email(c.getEmail())
                .token(token)
                .kycComplete(kycComplete)
                .crpComplete(crpComplete)
                .riskProfileType(riskProfileType)
                .build();
    }

    public void changePassword(UUID customerId, ChangePasswordRequest req) {
        MstCustomer c = customerRepository.findById(customerId).orElseThrow(() -> new BusinessException("AUTH_INVALID_CREDENTIALS"));
        if (!encoder.matches(req.getCurrentPassword(), c.getPasswordHash())) {
            throw new BusinessException("CURRENT_PASSWORD_INVALID");
        }
        if (!req.getNewPassword().equals(req.getConfirmNewPassword()) || !policyOk(req.getNewPassword())) {
            throw new BusinessException("PASSWORD_POLICY_VIOLATION");
        }
        c.setPasswordHash(encoder.encode(req.getNewPassword()));
        customerRepository.save(c);
    }

    public void updateRiskProfile(UUID customerId, UUID riskProfileId) {
        MstCustomer c = customerRepository.findById(customerId).orElseThrow(() -> new BusinessException("AUTH_INVALID_CREDENTIALS"));
        c.setIdRiskProfile(riskProfileId);
        customerRepository.save(c);
    }

    private boolean policyOk(String p) {
        if (p == null || p.length() < 8) return false;
        boolean hasLower=false, hasUpper=false, hasDigit=false;
        for (char ch : p.toCharArray()) {
            if (Character.isLowerCase(ch)) hasLower=true;
            if (Character.isUpperCase(ch)) hasUpper=true;
            if (Character.isDigit(ch)) hasDigit=true;
        }
        return hasLower && hasUpper && hasDigit;
    }

    // Kafka disabled: audit publish methods removed
}
