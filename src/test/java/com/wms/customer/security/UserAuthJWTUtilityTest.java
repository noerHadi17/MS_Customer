package com.wms.customer.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserAuthJWTUtility Unit Tests")
class UserAuthJWTUtilityTest {

    private UserAuthJWTUtility utility;

    @BeforeEach
    void setup() {
        utility = new UserAuthJWTUtility();
    }

    @Test
    @DisplayName("rsaPublicKey should parse PEM file successfully")
    void rsaPublicKey_ShouldParsePEMFileSuccessfully() throws Exception {
        // Act
        RSAPublicKey publicKey = utility.rsaPublicKey();

        // Assert
        assertThat(publicKey).isNotNull();
        assertThat(publicKey.getAlgorithm()).isEqualTo("RSA");
        assertThat(publicKey.getModulus().bitLength()).isGreaterThan(1024);
    }

    @Test
    @DisplayName("jwkSource should contain expected key ID")
    void jwkSource_ShouldContainExpectedKeyID() throws Exception {
        RSAPublicKey publicKey = utility.rsaPublicKey();
        JWKSource<SecurityContext> jwkSource = utility.jwkSource(publicKey);

        assertThat(jwkSource).isNotNull();

        JWKSet jwkSet = ((com.nimbusds.jose.jwk.source.ImmutableJWKSet<SecurityContext>) jwkSource).getJWKSet();
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwkSet.toJSONObject().get("keys");

        assertThat(keys).isNotEmpty();
        assertThat(keys.get(0).get("kid")).isEqualTo("wms-rsa-pub");
    }

    @Test
    @DisplayName("pemSupport should parse key manually")
    void pemSupport_ShouldParseKeyManually() throws Exception {
        String pem = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAt89BRv1MxTyLEFzLuw3N
            2p8TVX3YV8CvHqUOKPn6WGuIoht2kF6n9hWg0MZKf3R0o3+ZoxKQ4cWfVnsmkUtY
            Pa2SGQv1xXyd3zUjH1pX7I/kLM/OMkUnPYf13keb9P9fZ3zz8Jf0MQS/sQhWq6d8
            AW0dR0e4D3a/1yR17ZV4KfKDsFzUOu4D4WaaG/QpsnClm3DhqDukOOVExekbGp1l
            8lVwGvRLZ4gFUdMWLOZFT3kT4AfGi/tJ1K+S0gJ3muLMylQydpPoZsMIrO7gC6tX
            DTJbXx+AcGiyYv3f4j5VfIlcX9Pcsl4cFv8uOZ6jzN95VDBXLkLU8WR8q8Ow7h2w
            FwIDAQAB
            -----END PUBLIC KEY-----
            """;

        RSAPublicKey parsedKey = UserAuthJWTUtility.PemSupport.parseRSAPublicKey(pem);

        assertThat(parsedKey).isNotNull();
        assertThat(parsedKey.getAlgorithm()).isEqualTo("RSA");
    }
}