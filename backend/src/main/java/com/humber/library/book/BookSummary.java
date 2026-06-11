package com.humber.library.book;

import java.time.LocalDate;

public record BookSummary(
        long id,
        String isbn,
        String title,
        String author,
        String publisher,
        LocalDate publishDate,
        Long categoryId,
        String categoryName,
        String description,
        int totalCopies,
        int availableCopies) {
}
