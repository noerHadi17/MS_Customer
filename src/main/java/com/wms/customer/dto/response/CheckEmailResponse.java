package com.wms.customer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model indicating whether a given email is already associated with a customer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckEmailResponse {
    private boolean exists;
}
