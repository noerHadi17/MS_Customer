package com.wms.customer.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @JsonAlias({"oldPassword", "currentPassword"})
    @NotBlank private String currentPassword;
    @JsonAlias({"newPassword"})
    @NotBlank private String newPassword;
    @JsonAlias({"newPw", "confirmNewPassword"})
    @NotBlank private String confirmNewPassword;
}
