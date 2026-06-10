package com.humber.library.book;

import jakarta.validation.constraints.NotNull;

public record BookCopyStatusUpdateRequest(
        @NotNull(message = "馆藏副本状态不能为空")
        Status status) {

    public enum Status {
        AVAILABLE,
        LOST,
        DAMAGED,
        MAINTENANCE
    }
}
