package com.wms.customer.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KycRequest {
    @JsonProperty("nik")
    @NotBlank private String nik;
    @JsonProperty("pob")
    @NotBlank private String pob;
    @JsonProperty("dob")
    @NotNull private LocalDate dob;
}
