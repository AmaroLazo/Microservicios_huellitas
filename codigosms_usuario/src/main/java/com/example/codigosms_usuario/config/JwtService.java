package com.example.codigosms_usuario.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.codigosms_usuario.model.Role;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JwtService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final String HEADER = ENCODER.encodeToString(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final String ISSUER = "huellitas-usuarios";
    private static final String AUDIENCE = "huellitas-api";

    private final ObjectMapper json;
    private final SecretKeySpec key;

    public JwtService(ObjectMapper json, @Value("${HUELLITAS_JWT_SECRET}") String secret) {
        this.json = json;
        byte[] bytes = Base64.getDecoder().decode(secret);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("HUELLITAS_JWT_SECRET requiere al menos 32 bytes codificados en Base64");
        }
        this.key = new SecretKeySpec(bytes, "HmacSHA256");
    }

    public String issue(long userId, Role role) {
        Instant now = Instant.now();
        try {
            String payload = ENCODER.encodeToString(json.writeValueAsBytes(Map.of(
                    "iss", ISSUER, "aud", AUDIENCE, "sub", Long.toString(userId),
                    "role", role.name(), "iat", now.getEpochSecond(),
                    "exp", now.plusSeconds(3600).getEpochSecond())));
            String content = HEADER + "." + payload;
            return content + "." + ENCODER.encodeToString(sign(content));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el token", e);
        }
    }

    public Identity verify(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() > 4096) {
            throw new IllegalArgumentException("Se requiere un token");
        }
        String[] parts = authorization.substring(7).split("\\.", -1);
        if (parts.length != 3 || !HEADER.equals(parts[0])
                || !MessageDigest.isEqual(sign(parts[0] + "." + parts[1]), DECODER.decode(parts[2]))) {
            throw new IllegalArgumentException("Token inválido");
        }
        try {
            JsonNode claims = json.readTree(DECODER.decode(parts[1]));
            long expiration = claims.path("exp").asLong(0);
            long userId = Long.parseLong(claims.path("sub").asText());
            if (!ISSUER.equals(claims.path("iss").asText()) || !AUDIENCE.equals(claims.path("aud").asText())
                    || userId <= 0 || expiration <= Instant.now().getEpochSecond()) {
                throw new IllegalArgumentException("Token inválido o vencido");
            }
            return new Identity(userId, Role.valueOf(claims.path("role").asText()));
        } catch (IOException e) {
            throw new IllegalArgumentException("Token inválido", e);
        }
    }

    private byte[] sign(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return mac.doFinal(content.getBytes(StandardCharsets.US_ASCII));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo firmar el token", e);
        }
    }

    public record Identity(long userId, Role role) {}
}
