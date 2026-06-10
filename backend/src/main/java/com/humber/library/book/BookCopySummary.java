package com.humber.library.book;

import java.time.LocalDate;

public record BookCopySummary(
        long id,
        long bookId,
        String barcode,
        String shelfLocation,
        String status,
        LocalDate acquiredAt) {
}
