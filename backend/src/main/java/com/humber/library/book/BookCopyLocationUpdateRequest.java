package com.humber.library.book;

import jakarta.validation.constraints.Size;

public record BookCopyLocationUpdateRequest(
        @Size(max = 80, message = "书架位置不能超过 80 个字符")
        String shelfLocation) {
}
