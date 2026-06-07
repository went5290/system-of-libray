package com.humber.library.borrow;

import java.time.LocalDateTime;

public record BorrowCreateResponse(
        long id,
        String readerNo,
        String barcode,
        String bookTitle,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        String status) {
}

