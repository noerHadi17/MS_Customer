package com.wms.customer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "customer.defaults")
public class CustomerDefaultsProperties {
    private String nikPlaceholder = "-";
    private String pobPlaceholder = "-";
    private boolean defaultDobNow = true;
}

