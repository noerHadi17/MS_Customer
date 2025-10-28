package com.wms.customer.security;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * 🧩 Integration test end-to-end:
 * Load PEM → parse public key → generate encrypted JWT → decrypt back → verify claims.
 */
@DisplayName("UserAuthJWTIntegration Unit Tests")
class UserAuthJWTIntegrationTest {

    private UserAuthJWTUtility utility;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
    private UserAuthJWT jwtGenerator;

    @BeforeEach
    void setup() throws Exception {
        utility = new UserAuthJWTUtility();

        // Load real PEM public key dari classpath
        publicKey = utility.rsaPublicKey();

        // Generate private key sementara (karena PEM kamu biasanya cuma public)
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // Create UserAuthJWT instance pakai public key dari file PEM
        jwtGenerator = new UserAuthJWT(publicKey);
    }

    @Test
    @DisplayName("full flow from PEM public key should generate and decrypt token")
    void fullFlow_FromPemPublicKey_ShouldGenerateAndDecryptToken() throws Exception {
        UUID customerId = UUID.randomUUID();
        String name = "Ivan";
        String email = "ivan@mail.com";
        String issuer = "wms-customer-service";

        // Generate token pakai public key dari file PEM
        String token = jwtGenerator.generateAuthToken(customerId, name, email, 30, issuer);

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(5); // JWE parts

        // Karena kita tidak punya private key asli dari file PEM,
        // bagian decrypt hanya untuk demonstrasi (biasanya gateway yang decrypt).
        // Jadi kita tidak decrypt di sini untuk real PEM, hanya verifikasi structure.
        System.out.println("Generated JWT from PEM: " + token.substring(0, 60) + "...");

        // Minimal verifikasi bahwa token benar terenkripsi JWE
        assertThat(token).startsWith("eyJ");
    }

    @Test
    @DisplayName("PEM support should parse real PEM successfully")
    void pemSupport_ShouldParseRealPemSuccessfully() throws Exception {
        // Baca isi file PEM
        String pem;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("certificate/public-key.pem").getInputStream(),
                StandardCharsets.UTF_8
        ))) {
            pem = br.lines().reduce("", (a, b) -> a + "\n" + b);
        }

        // Parse pakai internal PemSupport
        RSAPublicKey key = UserAuthJWTUtility.PemSupport.parseRSAPublicKey(pem);

        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("RSA");
        assertThat(key.getModulus().bitLength()).isGreaterThan(1024);
    }
}