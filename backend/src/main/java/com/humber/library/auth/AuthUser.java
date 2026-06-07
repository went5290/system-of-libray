package com.humber.library.auth;

public record AuthUser(
        long id,
        String username,
        String passwordHash,
        String displayName,
        boolean enabled) {
}
