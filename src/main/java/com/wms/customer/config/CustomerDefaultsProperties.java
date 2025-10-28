package com.wms.customer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties that define default values used when customer data is incomplete.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "customer.defaults")
public class CustomerDefaultsProperties {
    private String nikPlaceholder = "-";
    private String pobPlaceholder = "-";
    private boolean defaultDobNow = true;

    private Integer jwtTtlMinutes = 360; // default 6 jam

    public Integer getJwtTtlMinutes() {
        return jwtTtlMinutes;
    }

    public void setJwtTtlMinutes(Integer jwtTtlMinutes) {
        this.jwtTtlMinutes = jwtTtlMinutes;
    }
}
