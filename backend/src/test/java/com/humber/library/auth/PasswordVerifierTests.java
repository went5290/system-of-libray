package com.humber.library.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordVerifierTests {
    private static final String HASH =
            "pbkdf2$120000$TiIXn/ioj++UBH3u56TneQ==$zPy1D8mVrkpDEeEdSqvvvjFhrcpy7A5QxEgVWX8DR3U=";

    private final PasswordVerifier passwordVerifier = new PasswordVerifier();

    @Test
    void verifiesMatchingPassword() {
        assertThat(passwordVerifier.matches("Aa233613", HASH)).isTrue();
    }

    @Test
    void rejectsWrongPassword() {
        assertThat(passwordVerifier.matches("wrong-password", HASH)).isFalse();
    }
}
