package com.wms.customer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

/**
 * Security configuration for the application.
 * ================================================================================
 * This configuration disables CSRF protection, disables form login and HTTP basic,
 * and registers a custom {@code GatewayHeaderFilter} before the {@link CorsFilter}.
 * Public endpoints for authentication, KYC and user operations are permitted while
 * all other requests require authentication.</p>
 * ================================================================================
 * Defined request matchers that are permitted:
 *   {@code /v1/auth/**}</li>
 *   {@code /v1/kyc/**}</li>
 *   {@code /v1/kyc-status}</li>
 *   {@code /v1/user/**}</li>
 * ================================================================================
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Builds the {@link SecurityFilterChain} used by Spring Security.
     * ================================================================================
     * The returned chain:
     * - disables CSRF
     * - permits unauthenticated access to authentication, KYC and user endpoints
     * - adds the provided {@code GatewayHeaderFilter} before the {@link CorsFilter}
     * - disables form login and HTTP Basic authentication
     * ================================================================================
     * @param http the {@link HttpSecurity} to modify
     * @param gatewayHeaderFilter the custom filter that processes gateway headers; added before the {@link CorsFilter}
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while building the security chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, GatewayHeaderFilter gatewayHeaderFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/**", "/v1/kyc/**", "/v1/kyc-status", "/v1/user/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayHeaderFilter, CorsFilter.class)
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
