package com.humber.library.reader;

import jakarta.validation.constraints.NotNull;

public record ReaderStatusUpdateRequest(
        @NotNull(message = "读者状态不能为空")
        ReaderStatus status) {

    public enum ReaderStatus {
        ACTIVE,
        SUSPENDED,
        CLOSED
    }
}

