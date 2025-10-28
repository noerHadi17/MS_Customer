package com.wms.customer.service.auth;

import com.wms.customer.config.CustomerDefaultsProperties;
import com.wms.customer.dto.request.ChangePasswordRequest;
import com.wms.customer.dto.request.LoginRequest;
import com.wms.customer.dto.request.RegisterRequest;
import com.wms.customer.dto.response.LoginResponse;
import com.wms.customer.dto.response.RegisterResponse;
import com.wms.customer.entity.MstCustomer;
import com.wms.customer.exception.BusinessException;
import com.wms.customer.kafka.AuditEventProducer;
import com.wms.customer.repository.MstCustomerRepository;
import com.wms.customer.repository.MstRiskProfileRefRepository;
import com.wms.customer.security.UserAuthJWT;
import com.wms.customer.security.UserAuthJWTUtility;
import com.wms.customer.service.implementation.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock private MstCustomerRepository customerRepository;
    @Mock private UserAuthJWTUtility userAuthJWTUtility;
    @Mock private UserAuthJWT userAuthJWT;
    @Mock private AuditEventProducer auditEventProducer;
    @Mock private CustomerDefaultsProperties defaults;
    @Mock private MstRiskProfileRefRepository riskProfileRepository;

    @InjectMocks private AuthServiceImpl authService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private MstCustomer dummyCustomer;

    @BeforeEach
    void setUp() {
        dummyCustomer = new MstCustomer();
        dummyCustomer.setCustomerId(UUID.randomUUID());
        dummyCustomer.setName("Ivan");
        dummyCustomer.setEmail("ivan@mail.com");
        dummyCustomer.setPasswordHash(encoder.encode("Pass123"));
        dummyCustomer.setNik("123456789");
        dummyCustomer.setPob("Jakarta");
    }

    @Test
    @DisplayName("checkEmail")
    void checkEmail_ShouldReturnTrue_WhenExists() {
        when(customerRepository.existsByEmail("ivan@mail.com")).thenReturn(true);
        assertThat(authService.checkEmail("ivan@mail.com")).isTrue();
    }

    @Test
    void checkEmail_ShouldReturnFalse_WhenNotExists() {
        when(customerRepository.existsByEmail("nonexist@mail.com")).thenReturn(false);
        assertThat(authService.checkEmail("nonexist@mail.com")).isFalse();
    }

    @Test
    @DisplayName("register success")
    void register_ShouldSaveCustomer_WhenEmailNotExists() {
        RegisterRequest req = new RegisterRequest("Ivan", "ivan@mail.com", "Pass123", "Jakarta");
        when(customerRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(defaults.getNikPlaceholder()).thenReturn("-");
        when(defaults.getPobPlaceholder()).thenReturn("-");
        when(defaults.isDefaultDobNow()).thenReturn(true);

        RegisterResponse res = authService.register(req);

        assertThat(res).isNotNull();
        assertThat(res.getEmail()).isEqualTo("ivan@mail.com");
        verify(customerRepository).save(any(MstCustomer.class));
        verify(auditEventProducer).sendAuditEvent(
                eq("REGISTER_SUCCESS"), any(), eq("ivan@mail.com"), eq("SUCCESS"), anyString());
    }

    @Test
    @DisplayName("register fail")
    void register_ShouldThrow_WhenEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest("Ivan", "ivan@mail.com", "Pass123", "Jakarta");
        when(customerRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("EMAIL_ALREADY_EXISTS");

        verify(auditEventProducer).sendAuditEvent(eq("REGISTER_FAILURE"), any(), eq("ivan@mail.com"), eq("FAILURE"), anyString());
    }

    @Test
    @DisplayName("login success")
    void login_ShouldReturnResponse_WhenCredentialsValid() {
        LoginRequest req = new LoginRequest("ivan@mail.com", "Pass123");
        when(customerRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(dummyCustomer));
        when(userAuthJWT.generateAuthToken(any(), any(), any(), anyInt(), anyString())).thenReturn("jwt-token");
        when(defaults.getJwtTtlMinutes()).thenReturn(360);

        LoginResponse res = authService.login(req);

        assertThat(res.getEmail()).isEqualTo("ivan@mail.com");
        assertThat(res.getToken()).isEqualTo("jwt-token");
        verify(auditEventProducer).sendAuditEvent(eq("LOGIN_SUCCESS"), any(), eq("ivan@mail.com"), eq("SUCCESS"), eq("Login"));
    }

    @Test
    @DisplayName("login fail")
    void login_ShouldThrow_WhenInvalidCredentials() {
        LoginRequest req = new LoginRequest("ivan@mail.com", "wrongpass");
        when(customerRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(dummyCustomer));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("AUTH_INVALID_CREDENTIALS");

        verify(auditEventProducer).sendAuditEvent(eq("LOGIN_FAILURE"), any(), eq("ivan@mail.com"), eq("FAILURE"), anyString());
    }

    @Test
    @DisplayName("changePassword success")
    void changePassword_ShouldUpdatePassword_WhenValid() {
        ChangePasswordRequest req = new ChangePasswordRequest("Pass123", "NewPass123", "NewPass123");
        when(customerRepository.findById(dummyCustomer.getCustomerId())).thenReturn(Optional.of(dummyCustomer));

        authService.changePassword(dummyCustomer.getCustomerId(), req);

        verify(customerRepository).save(any(MstCustomer.class));
    }

    @Test
    @DisplayName("changePassword invalid current password")
    void changePassword_ShouldThrow_WhenCurrentPasswordInvalid() {
        ChangePasswordRequest req = new ChangePasswordRequest("Wrong123", "NewPass123", "NewPass123");
        when(customerRepository.findById(dummyCustomer.getCustomerId())).thenReturn(Optional.of(dummyCustomer));

        assertThatThrownBy(() -> authService.changePassword(dummyCustomer.getCustomerId(), req))
                .isInstanceOf(BusinessException.class)
                .hasMessage("CURRENT_PASSWORD_INVALID");
    }

    @Test
    @DisplayName("updateRiskProfile")
    void updateRiskProfile_ShouldSaveCustomer() {
        UUID newRiskId = UUID.randomUUID();
        when(customerRepository.findById(dummyCustomer.getCustomerId())).thenReturn(Optional.of(dummyCustomer));

        authService.updateRiskProfile(dummyCustomer.getCustomerId(), newRiskId);

        verify(customerRepository).save(argThat(c -> c.getIdRiskProfile().equals(newRiskId)));
    }
}