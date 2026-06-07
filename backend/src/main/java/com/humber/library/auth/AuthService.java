package com.humber.library.auth;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final PasswordVerifier passwordVerifier;

    public AuthService(AuthRepository authRepository, PasswordVerifier passwordVerifier) {
        this.authRepository = authRepository;
        this.passwordVerifier = passwordVerifier;
    }

    public LoginResponse login(LoginRequest request) {
        AuthUser user = authRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new AuthFailedException("用户名或密码错误"));

        if (!user.enabled()) {
            throw new AuthFailedException("账号已停用");
        }
        if (!passwordVerifier.matches(request.password(), user.passwordHash())) {
            throw new AuthFailedException("用户名或密码错误");
        }

        return new LoginResponse(
                user.id(),
                user.username(),
                user.displayName(),
                authRepository.findRoleCodes(user.id()));
    }
}
