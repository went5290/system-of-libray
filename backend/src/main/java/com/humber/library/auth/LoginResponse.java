package com.humber.library.auth;

import java.util.List;
import java.time.Instant;

public record LoginResponse(
        long userId,
        String username,
        String displayName,
        List<String> roles,
        String token,
        Instant expiresAt) {
}
