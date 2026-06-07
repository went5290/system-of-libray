package com.humber.library.borrow;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BorrowRecordSummary(
        long id,
        String readerNo,
        String barcode,
        String bookTitle,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        LocalDateTime returnedAt,
        int renewCount,
        String status,
        Long fineId,
        BigDecimal fineAmount,
        BigDecimal paidAmount,
        String fineStatus) {
}
