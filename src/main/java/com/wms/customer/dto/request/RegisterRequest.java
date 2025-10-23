package com.wms.customer.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO describing the information required to create a new customer account.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @JsonProperty("name")
    @NotBlank private String name;
    @JsonProperty("email")
    @Email @NotBlank private String email;
    @JsonProperty("password")
    @NotBlank private String password;
    @JsonProperty("address")
    @NotBlank private String address;
}
