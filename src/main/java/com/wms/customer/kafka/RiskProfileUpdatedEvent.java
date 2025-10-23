package com.wms.customer.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Event payload published when a customer's risk profile changes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskProfileUpdatedEvent {
    private int schemaVersion;
    private UUID eventId;
    private String occurredAt;
    private UUID customerId;
    private UUID riskProfileId;
    private String riskProfileName;
    private int totalScore;
    private UUID questionnaireId;
}
