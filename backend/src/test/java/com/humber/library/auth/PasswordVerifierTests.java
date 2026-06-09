package com.humber.library.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordVerifierTests {
    private static final String HASH =
            "pbkdf2$120000$dGVzdC1zYWx0LTE2Ynl0ZQ==$YKpYm/G+kY4D34WenLXa/jn4AYoeeHybcwGY6GtjMaY=";

    private final PasswordVerifier passwordVerifier = new PasswordVerifier();

    @Test
    void verifiesMatchingPassword() {
        assertThat(passwordVerifier.matches("unit-test-password", HASH)).isTrue();
    }

    @Test
    void rejectsWrongPassword() {
        assertThat(passwordVerifier.matches("wrong-password", HASH)).isFalse();
    }
}
