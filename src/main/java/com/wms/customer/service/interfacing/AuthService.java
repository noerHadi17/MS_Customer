package com.wms.customer.service.interfacing;

import com.wms.customer.dto.request.ChangePasswordRequest;
import com.wms.customer.dto.request.LoginRequest;
import com.wms.customer.dto.request.RegisterRequest;
import com.wms.customer.dto.response.LoginResponse;
import com.wms.customer.dto.response.RegisterResponse;

import java.util.UUID;

/**
 * Defines authentication-related operations exposed by the customer service.
 */
public interface AuthService {
    boolean checkEmail(String email);
    RegisterResponse register(RegisterRequest req);
    LoginResponse login(LoginRequest req);
    void changePassword(UUID customerId, ChangePasswordRequest req);
    void updateRiskProfile(UUID customerId, UUID riskProfileId);
}
