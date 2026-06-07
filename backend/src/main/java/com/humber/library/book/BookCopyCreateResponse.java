package com.humber.library.book;

public record BookCopyCreateResponse(long id, long bookId, String barcode, String status) {
}

