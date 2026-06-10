package com.humber.library.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.OptionalLong;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenService {
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(8);
    private final byte[] secret;

    public TokenService(@Value("${library.auth.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public AuthToken issue(long userId) {
        Instant expiresAt = Instant.now().plus(TOKEN_LIFETIME);
        String payload = userId + ":" + expiresAt.getEpochSecond();
        return new AuthToken(encode(payload) + "." + encode(sign(payload)), expiresAt);
    }

    public boolean isValid(String token) {
        return userId(token).isPresent();
    }

    public OptionalLong userId(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                return OptionalLong.empty();
            }

            String payload = new String(decode(parts[0]), StandardCharsets.UTF_8);
            byte[] expectedSignature = sign(payload);
            byte[] actualSignature = decode(parts[1]);
            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                return OptionalLong.empty();
            }

            String[] payloadParts = payload.split(":");
            if (payloadParts.length != 2) {
                return OptionalLong.empty();
            }
            long userId = Long.parseLong(payloadParts[0]);
            boolean valid = userId > 0
                    && Instant.ofEpochSecond(Long.parseLong(payloadParts[1])).isAfter(Instant.now());
            return valid ? OptionalLong.of(userId) : OptionalLong.empty();
        } catch (IllegalArgumentException exception) {
            return OptionalLong.empty();
        }
    }

    private byte[] sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("无法生成登录令牌", exception);
        }
    }

    private String encode(String value) {
        return encode(value.getBytes(StandardCharsets.UTF_8));
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
