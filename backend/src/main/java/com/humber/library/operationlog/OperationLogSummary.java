package com.humber.library.operationlog;

import java.time.LocalDateTime;

public record OperationLogSummary(
        long id,
        String username,
        String displayName,
        String action,
        String targetType,
        String targetId,
        String detail,
        String ipAddress,
        LocalDateTime createdAt) {
}
