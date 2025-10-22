package com.wms.customer.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @JsonProperty("currentPassword")
    @NotBlank private String currentPassword;
    @JsonProperty("newPassword")
    @NotBlank private String newPassword;
    @JsonProperty("confirmNewPassword")
    @NotBlank private String confirmNewPassword;
}
