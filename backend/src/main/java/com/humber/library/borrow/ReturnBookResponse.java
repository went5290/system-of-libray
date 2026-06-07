package com.humber.library.borrow;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReturnBookResponse(
        long borrowId,
        String readerNo,
        String barcode,
        String bookTitle,
        LocalDateTime returnedAt,
        long overdueDays,
        BigDecimal fineAmount,
        String status) {
}

