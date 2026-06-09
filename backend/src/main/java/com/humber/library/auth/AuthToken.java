package com.humber.library.auth;

import java.time.Instant;

public record AuthToken(String value, Instant expiresAt) {
}
