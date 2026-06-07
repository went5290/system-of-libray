package com.humber.library.auth;

import java.util.List;

public record LoginResponse(
        long userId,
        String username,
        String displayName,
        List<String> roles) {
}
