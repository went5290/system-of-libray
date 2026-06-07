package com.humber.library.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookCopyCreateRequest(
        @NotBlank(message = "馆藏条码不能为空")
        @Size(max = 50, message = "馆藏条码不能超过 50 个字符")
        String barcode,

        @Size(max = 80, message = "书架位置不能超过 80 个字符")
        String shelfLocation) {
}

