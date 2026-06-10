package com.humber.library.operationlog;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class OperationLogService {
    private final OperationLogRepository operationLogRepository;

    public OperationLogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    public void record(String action, String targetType, long targetId, String detail) {
        HttpServletRequest request = currentRequest();
        Object userId = request.getAttribute("authenticatedUserId");
        if (userId instanceof Long id) {
            operationLogRepository.insert(
                    id,
                    action,
                    targetType,
                    Long.toString(targetId),
                    detail,
                    clientIp(request));
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr()
                : forwarded.split(",")[0].trim();
    }
}
