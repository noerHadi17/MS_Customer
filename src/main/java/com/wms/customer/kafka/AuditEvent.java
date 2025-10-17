package com.wms.customer.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {
    private UUID eventId;
    private String occurredAt; // ISO string
    private String serviceName;
    private String action;
    private String status;
    private UUID customerId;
    private String email;
    private String description;
}

