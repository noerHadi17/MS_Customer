package com.wms.customer.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias({"nik"})
    @NotBlank private String nik;
    @JsonAlias({"pob"})
    @NotBlank private String pob;
    @JsonAlias({"dob"})
    @NotNull private LocalDate dob;
}
