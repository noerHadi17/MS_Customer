package com.wms.customer.web;

import com.wms.customer.dto.request.*;
import com.wms.customer.dto.response.*;
import lombok.Data;
import com.wms.customer.service.AuthService;
import com.wms.customer.service.KycService;
import com.wms.customer.i18n.I18nMessageCollection;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    private final AuthService authService;
    private final KycService kycService;
    private final MessageSource messageSource;

    @PostMapping("/v1/user/check-email")
    public ResponseEntity<ResponseWrapper<Map<String, Object>>> checkEmail(@RequestBody @Valid CheckEmailRequest req, Locale locale) {
        log.info("[customer] check-email email={}", req.getEmail());
        boolean exists = authService.checkEmail(req.getEmail());
        String msg = I18nMessageCollection.EMAIL_CHECKED.localized(messageSource, locale);
        log.info("[customer] check-email email={} exists={}", req.getEmail(), exists);
        return ResponseEntity.ok(ApiResponseUtil.success(Map.of("exists", exists), I18nMessageCollection.EMAIL_CHECKED.name(), msg));
    }

    @PostMapping("/v1/auth/register")
    public ResponseEntity<ResponseWrapper<RegisterResponse>> register(@RequestBody @Valid RegisterRequest req, Locale locale) {
        log.info("[customer] register email={} name={}", req.getEmail(), req.getName());
        RegisterResponse res = authService.register(req);
        String msg = I18nMessageCollection.REGISTER_SUCCESS.localized(messageSource, locale);
        log.info("[customer] register success customerId={} email={}", res.getCustomerId(), res.getEmail());
        return ResponseEntity.ok(ApiResponseUtil.success(res, I18nMessageCollection.REGISTER_SUCCESS.name(), msg));

    }

    @PostMapping("/v1/auth/login")
    public ResponseEntity<ResponseWrapper<LoginResponse>> login(@RequestBody @Valid LoginRequest req, Locale locale) {
        log.info("[customer] login email={}", req.getEmail());
        LoginResponse res = authService.login(req);
        String msg = I18nMessageCollection.LOGIN_SUCCESS.localized(messageSource, locale);
        log.info("[customer] login success customerId={} kycComplete={}", res.getCustomerId(), res.isKycComplete());
        return ResponseEntity.ok(ApiResponseUtil.success(res, I18nMessageCollection.LOGIN_SUCCESS.name(), msg));
    }

    @PostMapping("/v1/auth/change-password")
    public ResponseEntity<ResponseWrapper<Void>> changePassword(@RequestHeader("X-User-Id") UUID userId, @RequestBody @Valid ChangePasswordRequest req, Locale locale) {
        log.info("[customer] change-password userId={}", userId);
        authService.changePassword(userId, req);
        String msg = I18nMessageCollection.CHANGE_PASSWORD_SUCCESS.localized(messageSource, locale);
        return ResponseEntity.ok(ApiResponseUtil.success(null, I18nMessageCollection.CHANGE_PASSWORD_SUCCESS.name(), msg));
    }

    @GetMapping("/v1/kyc/status")
    public ResponseEntity<ResponseWrapper<KycResponse>> kycStatus(@RequestHeader("X-User-Id") UUID userId, Locale locale) {
        log.info("[customer] kyc-status userId={}", userId);
        KycResponse res = kycService.getStatus(userId);
        String msg = I18nMessageCollection.EMAIL_CHECKED.localized(messageSource, locale);
        return ResponseEntity.ok(ApiResponseUtil.success(res, null, null));
    }

    @PostMapping("/v1/kyc")
    public ResponseEntity<ResponseWrapper<KycResponse>> kycSubmit(@RequestHeader("X-User-Id") UUID userId, @RequestBody @Valid KycRequest req, Locale locale) {
        log.info("[customer] kyc-submit userId={} nik={} pob={} dob={}", userId, req.getNik(), req.getPob(), req.getDob());
        KycResponse res = kycService.submit(userId, req);
        String msg = I18nMessageCollection.KYC_SUBMIT_SUCCESS.localized(messageSource, locale);
        return ResponseEntity.ok(ApiResponseUtil.success(res, I18nMessageCollection.KYC_SUBMIT_SUCCESS.name(), msg));
    }

    // Internal endpoint for CRP to update customer's risk profile FK
    @PostMapping("/v1/user/risk-profile")
    public ResponseEntity<ResponseWrapper<Void>> updateRiskProfile(@RequestBody @Valid UpdateRiskProfileRequest req, Locale locale) {
        log.info("[customer] update-risk-profile customerId={} riskProfileId={}", req.getCustomerId(), req.getRiskProfileId());
        authService.updateRiskProfile(req.getCustomerId(), req.getRiskProfileId());
        return ResponseEntity.ok(ApiResponseUtil.success(null, null, null));
    }

    @Data
    public static class UpdateRiskProfileRequest {
        @jakarta.validation.constraints.NotNull
        private UUID customerId;
        @jakarta.validation.constraints.NotNull
        private UUID riskProfileId;
    }
}
