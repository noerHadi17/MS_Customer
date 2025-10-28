package com.wms.customer.web;

import com.wms.customer.dto.request.*;
import com.wms.customer.dto.response.*;
import com.wms.customer.service.interfacing.AuthService;
import com.wms.customer.service.interfacing.KycService;
import com.wms.customer.i18n.I18nMessageCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController Unit Tests")
class CustomerControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private KycService kycService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private CustomerController customerController;

    private Locale locale;

    @BeforeEach
    void setUp() {
        locale = Locale.ENGLISH;
    }

    // Helper method to handle code comparison - adjust based on actual ResponseWrapper.getCode() return type
    private void assertCodeEquals(String expected, Object actual) {
        if (actual instanceof String) {
            assertEquals(expected, actual);
        } else if (actual instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<String> list = (java.util.List<String>) actual;
            assertFalse(list.isEmpty(), "Code list should not be empty");
            assertEquals(expected, list.get(0));
        } else {
            fail("Unexpected code type: " + (actual != null ? actual.getClass() : "null"));
        }
    }

    @Test
    @DisplayName("Should check email and return exists=true when email exists")
    void checkEmail_WhenEmailExists_ShouldReturnTrue() {
        // Arrange
        CheckEmailRequest request = new CheckEmailRequest();
        request.setEmail("test@example.com");

        when(authService.checkEmail(anyString())).thenReturn(true);
        when(messageSource.getMessage(any(), any(), any(Locale.class)))
                .thenReturn("Email checked successfully");

        // Act
        ResponseEntity<ResponseWrapper<Map<String, Object>>> response =
                customerController.checkEmail(request, locale);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(true, response.getBody().getData().get("exists"));
        assertCodeEquals(I18nMessageCollection.EMAIL_CHECKED.name(), response.getBody().getMessageCodes());

        verify(authService, times(1)).checkEmail("test@example.com");
    }

    @Test
    @DisplayName("Should check email and return exists=false when email does not exist")
    void checkEmail_WhenEmailDoesNotExist_ShouldReturnFalse() {
        // Arrange
        CheckEmailRequest request = new CheckEmailRequest();
        request.setEmail("nonexistent@example.com");

        when(authService.checkEmail(anyString())).thenReturn(false);
        when(messageSource.getMessage(any(), any(), any(Locale.class)))
                .thenReturn("Email checked successfully");

        // Act
        ResponseEntity<ResponseWrapper<Map<String, Object>>> response =
                customerController.checkEmail(request, locale);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().getData().get("exists"));

        verify(authService, times(1)).checkEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should register user successfully with all required fields")
    void register_WithValidRequest_ShouldReturnRegisterResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setName("John Doe");
        request.setPassword("SecurePass123!");
        request.setAddress("123 Main Street, Jakarta");

        UUID customerId = UUID.randomUUID();
        RegisterResponse mockResponse = new RegisterResponse();
        mockResponse.setCustomerId(customerId);
        mockResponse.setEmail("newuser@example.com");
        mockResponse.setName("John Doe");
        mockResponse.setPassword("SecurePass123!");
        mockResponse.setAddress("123 Main Street, Jakarta");

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);
        when(messageSource.getMessage(any(), any(), any(Locale.class)))
                .thenReturn("Registration successful");

        // Act
        ResponseEntity<ResponseWrapper<RegisterResponse>> response =
                customerController.register(request, locale);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(customerId, response.getBody().getData().getCustomerId());
        assertEquals("newuser@example.com", response.getBody().getData().getEmail());
        assertEquals("John Doe", response.getBody().getData().getName());
        assertEquals("123 Main Street, Jakarta", response.getBody().getData().getAddress());
        assertCodeEquals(I18nMessageCollection.REGISTER_SUCCESS.name(), response.getBody().getMessageCodes());

        verify(authService, times(1)).register(request);
    }

    @Test
    @DisplayName("Should login user successfully with complete profile information")
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123!");

        UUID customerId = UUID.randomUUID();
        LoginResponse mockResponse = new LoginResponse();
        mockResponse.setCustomerId(customerId);
        mockResponse.setName("John Doe");
        mockResponse.setEmail("user@example.com");
        mockResponse.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        mockResponse.setKycComplete(true);
        mockResponse.setCrpComplete(true);
        mockResponse.setRiskProfileType("MODERATE");

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);
        when(messageSource.getMessage(any(), any(), any(Locale.class)))
                .thenReturn("Login successful");

        // Act
        ResponseEntity<ResponseWrapper<LoginResponse>> response =
                customerController.login(request, locale);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(customerId, response.getBody().getData().getCustomerId());
        assertEquals("user@example.com", response.getBody().getData().getEmail());
        assertTrue(response.getBody().getData().isKycComplete());
        assertTrue(response.getBody().getData().isCrpComplete());
        assertEquals("MODERATE", response.getBody().getData().getRiskProfileType());
        assertNotNull(response.getBody().getData().getToken());
        assertCodeEquals(I18nMessageCollection.LOGIN_SUCCESS.name(), response.getBody().getMessageCodes());

        verify(authService, times(1)).login(request);
    }

    @Test
    @DisplayName("Should login user with incomplete KYC")
    void login_WithIncompleteKyc_ShouldReturnKycCompleteAsFalse() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123!");

        UUID customerId = UUID.randomUUID();
        LoginResponse mockResponse = new LoginResponse();
        mockResponse.setCustomerId(customerId);
        mockResponse.setName("Jane Doe");
        mockResponse.setEmail("user@example.com");
        mockResponse.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        mockResponse.setKycComplete(false);
        mockResponse.setCrpComplete(false);
        mockResponse.setRiskProfileType(null);

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);
        when(messageSource.getMessage(any(), any(), any(Locale.class)))
                .thenReturn("Login successful");

        // Act
        ResponseEntity<ResponseWrapper<LoginResponse>> response =
                customerController.login(request, locale);

        // Assert
        assertNotNull(response);
        assertFalse(response.getBody().getData().isKycComplete());
        assertFalse(response.getBody().getData().isCrpComplete());
        assertNull(response.getBody().getData().getRiskProfileType());

        verify(authService, times(1)).login(request);
    }

    @Test
    @DisplayName("Should change password successfully with all password fields")
    void changePassword_WithValidRequest_ShouldReturnSuccess() {
        // Arrange
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPass123!");
        request.setNewPassword("NewPass456!");
        request.setConfirmNewPassword("NewPass456!");

        doNothing().when(authService).changePassword(any(UUID.class), any(ChangePasswordRequest.class));
        when(messageSource.getMessage(any(), any(), any(Locale.class)))
                .thenReturn("Password changed successfully");

        // Act
        ResponseEntity<ResponseWrapper<Void>> response =
                customerController.changePassword(userId, request, locale);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertCodeEquals(I18nMessageCollection.CHANGE_PASSWORD_SUCCESS.name(), response.getBody().getMessageCodes());

        verify(authService, times(1)).changePassword(userId, request);
    }

    @Test
    @DisplayName("Should get KYC status with pending status")
    void kycStatus_WithValidUserId_ShouldReturnKycResponse() {
        // Arrange
        UUID userId = UUID.randomUUID();
        KycResponse mockResponse = new KycResponse();
        mockResponse.setKycStatus("PENDING");
        mockResponse.setNik(null);
        mockResponse.setPob(null);
        mockResponse.setDob(null);

        when(kycService.getStatus(any(UUID.class))).thenReturn(mockResponse);
        when(messageSource.getMessage(any(), any(), any(Locale.class)))
                .thenReturn("KYC status retrieved");

        // Act
        ResponseEntity<ResponseWrapper<KycResponse>> response =
                customerController.kycStatus(userId, locale);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("PENDING", response.getBody().getData().getKycStatus());
        assertNull(response.getBody().getData().getNik());

        verify(kycService, times(1)).getStatus(userId);
    }

    @Test
    @DisplayName("Should get KYC status with approved status and data")
    void kycStatus_WithApprovedKyc_ShouldReturnCompleteKycData() {
        // Arrange
        UUID userId = UUID.randomUUID();
        KycResponse mockResponse = new KycResponse();
        mockResponse.setKycStatus("APPROVED");
        mockResponse.setNik("1234567890123456");
        mockResponse.setPob("Jakarta");
        mockResponse.setDob("1990-01-01");

        when(kycService.getStatus(any(UUID.class))).thenReturn(mockResponse);

        // Act
        ResponseEntity<ResponseWrapper<KycResponse>> response =
                customerController.kycStatus(userId, locale);

        // Assert
        assertNotNull(response);
        assertEquals("APPROVED", response.getBody().getData().getKycStatus());
        assertEquals("1234567890123456", response.getBody().getData().getNik());
        assertEquals("Jakarta", response.getBody().getData().getPob());
        assertEquals("1990-01-01", response.getBody().getData().getDob());

        verify(kycService, times(1)).getStatus(userId);
    }

    @Test
    @DisplayName("Should submit KYC with all required fields")
    void kycSubmit_WithValidRequest_ShouldReturnKycResponse() {
        // Arrange
        UUID userId = UUID.randomUUID();
        KycRequest request = new KycRequest();
        request.setNik("1234567890123456");
        request.setPob("Jakarta");
        request.setDob(LocalDate.of(1990, 1, 1));

        KycResponse mockResponse = new KycResponse();
        mockResponse.setKycStatus("APPROVED");
        mockResponse.setNik("1234567890123456");
        mockResponse.setPob("Jakarta");
        mockResponse.setDob("1990-01-01");

        when(kycService.submit(any(UUID.class), any(KycRequest.class))).thenReturn(mockResponse);
        when(messageSource.getMessage(any(), any(), any(Locale.class)))
                .thenReturn("KYC submitted successfully");

        // Act
        ResponseEntity<ResponseWrapper<KycResponse>> response =
                customerController.kycSubmit(userId, request, locale);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("APPROVED", response.getBody().getData().getKycStatus());
        assertEquals("1234567890123456", response.getBody().getData().getNik());
        assertEquals("Jakarta", response.getBody().getData().getPob());
        assertEquals("1990-01-01", response.getBody().getData().getDob());
        assertCodeEquals(I18nMessageCollection.KYC_SUBMIT_SUCCESS.name(), response.getBody().getMessageCodes());

        verify(kycService, times(1)).submit(userId, request);
    }

    @Test
    @DisplayName("Should update risk profile successfully")
    void updateRiskProfile_WithValidRequest_ShouldReturnSuccess() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID riskProfileId = UUID.randomUUID();

        CustomerController.UpdateRiskProfileRequest request =
                new CustomerController.UpdateRiskProfileRequest();
        request.setCustomerId(customerId);
        request.setRiskProfileId(riskProfileId);

        doNothing().when(authService).updateRiskProfile(any(UUID.class), any(UUID.class));

        // Act
        ResponseEntity<ResponseWrapper<Void>> response =
                customerController.updateRiskProfile(request, locale);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNull(response.getBody().getData());
        assertNull(response.getBody().getMessageCodes());
        assertNull(response.getBody().getMessageCodes());

        verify(authService, times(1)).updateRiskProfile(customerId, riskProfileId);
    }

    @Test
    @DisplayName("Should handle service exception during email check")
    void checkEmail_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        CheckEmailRequest request = new CheckEmailRequest();
        request.setEmail("error@example.com");

        when(authService.checkEmail(anyString()))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                customerController.checkEmail(request, locale));

        assertEquals("Database connection error", exception.getMessage());
        verify(authService, times(1)).checkEmail("error@example.com");
    }

    @Test
    @DisplayName("Should handle service exception during registration")
    void register_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@example.com");
        request.setName("Test User");
        request.setPassword("Password123!");
        request.setAddress("Test Address");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                customerController.register(request, locale));

        assertEquals("Email already exists", exception.getMessage());
        verify(authService, times(1)).register(request);
    }

    @Test
    @DisplayName("Should handle service exception during login")
    void login_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@example.com");
        request.setPassword("WrongPassword123!");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                customerController.login(request, locale));

        assertEquals("Invalid credentials", exception.getMessage());
        verify(authService, times(1)).login(request);
    }

    @Test
    @DisplayName("Should handle service exception during password change")
    void changePassword_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("WrongCurrentPass!");
        request.setNewPassword("NewPass456!");
        request.setConfirmNewPassword("NewPass456!");

        doThrow(new RuntimeException("Current password is incorrect"))
                .when(authService).changePassword(any(UUID.class), any(ChangePasswordRequest.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                customerController.changePassword(userId, request, locale));

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(authService, times(1)).changePassword(userId, request);
    }

    @Test
    @DisplayName("Should handle service exception during KYC submission")
    void kycSubmit_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        KycRequest request = new KycRequest();
        request.setNik("1234567890123456");
        request.setPob("Jakarta");
        request.setDob(LocalDate.of(1990, 1, 1));

        when(kycService.submit(any(UUID.class), any(KycRequest.class)))
                .thenThrow(new RuntimeException("NIK already registered"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                customerController.kycSubmit(userId, request, locale));

        assertEquals("NIK already registered", exception.getMessage());
        verify(kycService, times(1)).submit(userId, request);
    }

    @Test
    @DisplayName("Should verify header X-User-Id is passed correctly to change password")
    void changePassword_ShouldPassUserIdFromHeader() {
        // Arrange
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("OldPass123!");
        request.setNewPassword("NewPass456!");
        request.setConfirmNewPassword("NewPass456!");

        doNothing().when(authService).changePassword(eq(userId), any(ChangePasswordRequest.class));
        when(messageSource.getMessage(any(), any(), any(Locale.class)))
                .thenReturn("Password changed successfully");

        // Act
        customerController.changePassword(userId, request, locale);

        // Assert
        verify(authService, times(1)).changePassword(
                eq(UUID.fromString("550e8400-e29b-41d4-a716-446655440000")),
                eq(request)
        );
    }

    @Test
    @DisplayName("Should verify header X-User-Id is passed correctly to KYC endpoints")
    void kycSubmit_ShouldPassUserIdFromHeader() {
        // Arrange
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        KycRequest request = new KycRequest();
        request.setNik("1234567890123456");
        request.setPob("Jakarta");
        request.setDob(LocalDate.of(1990, 1, 1));

        KycResponse mockResponse = new KycResponse();
        mockResponse.setKycStatus("PENDING");

        when(kycService.submit(eq(userId), any(KycRequest.class))).thenReturn(mockResponse);
        when(messageSource.getMessage(any(), any(), any(Locale.class)))
                .thenReturn("KYC submitted successfully");

        // Act
        customerController.kycSubmit(userId, request, locale);

        // Assert
        verify(kycService, times(1)).submit(
                eq(UUID.fromString("550e8400-e29b-41d4-a716-446655440000")),
                eq(request)
        );
    }
}