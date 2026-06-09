package com.humber.library.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenServiceTests {
    private final TokenService tokenService = new TokenService("test-secret-with-enough-random-looking-characters");

    @Test
    void acceptsIssuedToken() {
        assertThat(tokenService.isValid(tokenService.issue(1).value())).isTrue();
    }

    @Test
    void rejectsModifiedToken() {
        String token = tokenService.issue(1).value();
        assertThat(tokenService.isValid(token + "changed")).isFalse();
    }
}
