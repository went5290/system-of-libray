package com.humber.library.book;

public record BookSummary(
        long id,
        String isbn,
        String title,
        String author,
        String publisher,
        String categoryName,
        int totalCopies,
        int availableCopies) {
}

