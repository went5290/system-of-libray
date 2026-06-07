package com.humber.library.borrow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BorrowCreateRequest(
        @NotBlank(message = "借书证号不能为空")
        @Size(max = 30, message = "借书证号不能超过 30 个字符")
        String readerNo,

        @NotBlank(message = "馆藏条码不能为空")
        @Size(max = 50, message = "馆藏条码不能超过 50 个字符")
        String barcode) {
}

