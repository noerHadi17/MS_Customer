package com.wms.customer.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.stream.Collectors;

/**
 * Utility untuk memuat kunci RSA dari classpath dan menyiapkan JWKSource
 * (kalau nanti gateway butuh JWKSet endpoint / decoder tinggal reuse).
 */
@Configuration
public class UserAuthJWTUtility {

    // letakkan file PEM di: src/main/resources/jwt/public.pem
    private static final String PUBLIC_KEY_PEM_PATH = "certificate/public-key.pem";

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        String pem = readKeyFromFile(PUBLIC_KEY_PEM_PATH);
        return PemSupport.parseRSAPublicKey(pem);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAPublicKey publicKey) {
        RSAKey rsaKey = new RSAKey.Builder(publicKey).keyID("wms-rsa-pub").build();
        JWKSet jwkSet = new JWKSet(java.util.Collections.singletonList(rsaKey));
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static String readKeyFromFile(String path) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * Parser pemula untuk RSAPublicKey (PEM) tanpa dependensi tambahan.
     * Jika kamu sudah pakai util sendiri, boleh diganti.
     */
    static final class PemSupport {
        static RSAPublicKey parseRSAPublicKey(String pem) throws Exception {
            String normalized = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = java.util.Base64.getDecoder().decode(normalized);
            java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(der);
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        }
    }
}
