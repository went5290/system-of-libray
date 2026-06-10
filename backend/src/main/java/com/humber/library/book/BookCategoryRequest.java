package com.humber.library.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookCategoryRequest(
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 80, message = "分类名称不能超过 80 个字符")
        String categoryName,

        @Size(max = 500, message = "分类描述不能超过 500 个字符")
        String description) {
}
