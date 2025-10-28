package com.wms.customer.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload used when a customer attempts to sign in with email credentials.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @JsonProperty("email")
    @Email @NotBlank private String email;
    @JsonProperty("password")
    @NotBlank private String password;
}
