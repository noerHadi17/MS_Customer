package com.wms.customer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that ensures incoming requests are routed through the API gateway.
 * =================================================================================
 * The filter permits requests to authentication endpoints (paths starting with {@code /v1/auth/})
 * and otherwise validates that the request originates from the configured gateway host/port or
 * includes the gateway port in forwarding headers.
 * =================================================================================
 * If validation fails the filter responds with HTTP {@code 403 Forbidden} and a short message.
 */
@Slf4j
@Component
public class GatewayHeaderFilter extends OncePerRequestFilter {

    /**
     * Expected gateway host name or IP address as configured in application properties:
     * {@code gateway.allowed-host}.
     */
    @Value("${gateway.allowed-host}")
    private String allowedHost;
    /**
     * Expected gateway port as configured in application properties:
     * {@code gateway.allowed-port}.
     */
    @Value("${gateway.allowed-port}")
    private Integer allowedPort;

    /**
     * Performs per-request filtering to ensure the request is proxied via the API gateway.
     * =================================================================================
     * <p>The method will skip filtering for authentication endpoints (paths starting with
     * {@code /v1/auth/}). For other requests it checks headers {@code X-Forwarded-Host} and
     * {@code Referer}, and the remote host/port of the connection. If the request is not
     * identified as coming from the gateway a {@code 403 Forbidden} response is returned.</p>
     * =================================================================================
     * @param request the incoming HTTP servlet request
     * @param response the HTTP servlet response
     * @param filterChain the filter chain to continue processing when validation passes
     * @throws ServletException if an exception occurs that interferes with the filter's operation
     * @throws IOException if an I/O error occurs while writing the response
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip authentication endpoints
        if (path.startsWith("/v1/auth/" )
                || path.startsWith("/v1/user/" )
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get headers and connection info
        String xForwardedHost = request.getHeader("X-Forwarded-Host");
        String referer = request.getHeader("Referer");
        String remoteHost = request.getRemoteHost();
        int remotePort = request.getRemotePort();

        // Validate: Check if request comes from gateway
        boolean fromGateway = isFromGateway(remoteHost, remotePort, xForwardedHost, referer);

        if (!fromGateway) {
            log.warn("🚫 Blocked direct access to {} {} | From {}:{} | X-Forwarded-Host={} | Referer={} (expected {}:{})",
                    method, path, remoteHost, remotePort, xForwardedHost, referer, allowedHost, allowedPort);

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Forbidden: Access must go through API Gateway");
            return;
        }

        // ✅ Validation passed
        filterChain.doFilter(request, response);
    }
    /**
     * Determines whether the request originated from the configured gateway.
     * =================================================================================
     * The method returns {@code true} if any of the following are met:
     * =================================================================================
     * The {@code X-Forwarded-Host} or {@code Referer} header contains the configured gateway port
     * The remote host matches the configured gateway host (or localhost loopback addresses) and the remote port matches
     * =================================================================================
     * @param remoteHost the remote host name or IP of the client connection
     * @param remotePort the remote port of the client connection
     * @param xForwardedHost the value of the {@code X-Forwarded-Host} header, may be {@code null}
     * @param referer the value of the {@code Referer} header, may be {@code null}
     * @return {@code true} when the request is considered to come from the gateway; {@code false} otherwise
     */
    private boolean isFromGateway(String remoteHost, int remotePort,
                                  String xForwardedHost, String referer) {
        boolean headerHasGatewayPort =
                (xForwardedHost != null && xForwardedHost.contains(String.valueOf(allowedPort))) ||
                        (referer != null && referer.contains(String.valueOf(allowedPort)));

        boolean isAllowedHost = remoteHost.equals(allowedHost) ||
                "127.0.0.1".equals(remoteHost) ||
                "0:0:0:0:0:0:0:1".equals(remoteHost) ||
                "localhost".equals(remoteHost);

        boolean isAllowedPort = (remotePort == allowedPort);

        return headerHasGatewayPort || (isAllowedHost && isAllowedPort);
    }
}