package com.humber.library.borrow;

import java.time.LocalDateTime;

public record BorrowRenewResponse(
        long id,
        String readerNo,
        String barcode,
        String bookTitle,
        LocalDateTime dueAt,
        int renewCount,
        String status) {
}

