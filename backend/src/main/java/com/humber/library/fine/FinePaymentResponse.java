package com.humber.library.fine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinePaymentResponse(
        long id,
        long borrowRecordId,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        String status,
        LocalDateTime paidAt) {
}

