package com.wms.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response returned after a new customer record is created.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private UUID customerId;
    private String name;
    private String email;
    private String password;
    private String address;
}
