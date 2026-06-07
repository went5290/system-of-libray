package com.humber.library.reader;

import java.time.LocalDateTime;

public record ReaderSummary(
        long id,
        String readerNo,
        String phone,
        String email,
        int maxBorrowCount,
        String status,
        LocalDateTime createdAt) {
}

