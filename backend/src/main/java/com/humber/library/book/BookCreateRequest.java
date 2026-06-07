package com.humber.library.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookCreateRequest(
        @NotBlank(message = "ISBN 不能为空")
        @Size(max = 20, message = "ISBN 不能超过 20 个字符")
        String isbn,

        @NotBlank(message = "书名不能为空")
        @Size(max = 200, message = "书名不能超过 200 个字符")
        String title,

        @Size(max = 150, message = "作者不能超过 150 个字符")
        String author,

        @Size(max = 150, message = "出版社不能超过 150 个字符")
        String publisher,

        LocalDate publishDate,

        @NotNull(message = "图书分类不能为空")
        Long categoryId,

        @Size(max = 1000, message = "图书简介不能超过 1000 个字符")
        String description) {
}

