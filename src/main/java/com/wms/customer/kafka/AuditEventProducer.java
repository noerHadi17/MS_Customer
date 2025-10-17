package com.wms.customer.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true")
public class AuditEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public void sendAuditEvent(String action, String customerId, String email, String status, String description) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", java.util.UUID.randomUUID());
            payload.put("occurredAt", java.time.OffsetDateTime.now().toString());
            payload.put("serviceName", "customer-service");
            payload.put("action", action);
            payload.put("customerId", customerId);
            payload.put("email", email);
            payload.put("status", status);
            payload.put("description", description);
            String json = mapper.writeValueAsString(payload);
            log.info("Sending audit event: {}", json);
            kafkaTemplate.send("audit.events", payload).whenComplete((result, ex) -> {
                if (ex == null) {
                    if (result != null && result.getRecordMetadata() != null) {
                        var md = result.getRecordMetadata();
                        log.info("Audit event sent: topic={}, partition={}, offset={}", md.topic(), md.partition(), md.offset());
                    } else {
                        log.info("Audit event sent");
                    }
                } else {
                    log.warn("Audit event send failed: {}", ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.warn("Failed to send audit event: {}", e.getMessage());
        }
    }
}
