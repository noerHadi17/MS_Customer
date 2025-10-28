package com.wms.customer.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Generator JWE (RSA-OAEP-256 + A256GCM) untuk token login.
 * Gateway harus bisa mendekripsi / memverifikasi format ini.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthJWT {

    private final RSAPublicKey rsaPublicKey; // dari UserAuthJWTUtility

    /**
     * Generate token terenkripsi untuk login.
     *
     * @param customerId UUID customer
     * @param name       nama user
     * @param email      email user
     * @param ttlMinutes  TTL token (menit)
     * @param issuer     string issuer (mis. "wms-customer-service")
     */
    public String generateAuthToken(UUID customerId,
                                    String name,
                                    String email,
                                    int ttlMinutes,
                                    String issuer) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(ttlMinutes * 60L);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(customerId.toString())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exp))
                    .claim("name", name)
                    .claim("email", email)
                    .build();

            JWEHeader header = new JWEHeader.Builder(
                    JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM
            ).contentType("JWT") // Optional header parameter for nested JWT
                    .build();

            EncryptedJWT encryptedJWT = new EncryptedJWT(header, claims);
            RSAEncrypter encrypter = new RSAEncrypter(rsaPublicKey);

            encryptedJWT.encrypt(encrypter);
            log.info("[JWT GEN] subject={}", customerId);
            log.info("[JWT GEN] claims={}", claims.toJSONObject());
            return encryptedJWT.serialize();

        } catch (Exception e) {
            // Biar jelas kalau ada masalah kunci / enkripsi
            throw new IllegalStateException("Failed to generate encrypted JWT", e);
        }
    }
}