package com.wms.customer.security;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserAuthJWT Unit Tests")
class UserAuthJWTTest {

    private UserAuthJWT userAuthJWT;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        // Generate RSA keypair sementara untuk unit test
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        userAuthJWT = new UserAuthJWT(publicKey);
    }

    @Test
    @DisplayName("generateAuthToken should return valid encrypted token")
    void generateAuthToken_ShouldReturnValidEncryptedToken() throws Exception {
        UUID customerId = UUID.randomUUID();
        String name = "Ivan";
        String email = "ivan@mail.com";
        String issuer = "wms-customer-service";
        int ttlMinutes = 60;

        String token = userAuthJWT.generateAuthToken(customerId, name, email, ttlMinutes, issuer);

        assertThat(token).isNotNull();
        assertThat(token).contains(".");

        // Dekripsi token pakai private key
        JWEObject jweObject = JWEObject.parse(token);
        jweObject.decrypt(new RSADecrypter(privateKey));

        String payload = jweObject.getPayload().toString();
        System.out.println("Decrypted Payload: " + payload);

        assertThat(payload).contains(name);
        assertThat(payload).contains(email);
        assertThat(payload).contains(issuer);
    }

    @Test
    @DisplayName("generateAuthToken should throw exception when key invalid")
    void generateAuthToken_ShouldThrowException_WhenKeyInvalid() {
        // Test edge case: jika key tidak bisa digunakan untuk enkripsi
        UserAuthJWT invalidJwt = new UserAuthJWT(null);
        UUID customerId = UUID.randomUUID();

        assertThatThrownBy(() ->
                invalidJwt.generateAuthToken(customerId, "Ivan", "ivan@mail.com", 60, "issuer")
        ).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to generate encrypted JWT");
    }
}