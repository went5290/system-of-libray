package com.humber.library.auth;

import com.humber.library.operationlog.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final PasswordVerifier passwordVerifier;
    private final TokenService tokenService;
    private final OperationLogService operationLogService;

    public AuthService(
            AuthRepository authRepository,
            PasswordVerifier passwordVerifier,
            TokenService tokenService,
            OperationLogService operationLogService) {
        this.authRepository = authRepository;
        this.passwordVerifier = passwordVerifier;
        this.tokenService = tokenService;
        this.operationLogService = operationLogService;
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

        AuthToken token = tokenService.issue(user.id());
        return new LoginResponse(
                user.id(),
                user.username(),
                user.displayName(),
                authRepository.findRoleCodes(user.id()),
                token.value(),
                token.expiresAt());
    }

    @Transactional
    public void changePassword(long userId, ChangePasswordRequest request) {
        AuthUser user = authRepository.findById(userId)
                .orElseThrow(() -> new AuthFailedException("当前账号不存在"));
        if (!passwordVerifier.matches(request.currentPassword(), user.passwordHash())) {
            throw new IllegalArgumentException("当前密码错误");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }
        if (request.currentPassword().equals(request.newPassword())) {
            throw new IllegalArgumentException("新密码不能与当前密码相同");
        }

        authRepository.updatePassword(user.id(), passwordVerifier.encode(request.newPassword()));
        operationLogService.record("CHANGE_PASSWORD", "SYS_USER", user.id(), "管理员修改登录密码");
    }
}
