package com.humber.library.borrow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReturnBookRequest(
        @NotBlank(message = "馆藏条码不能为空")
        @Size(max = 50, message = "馆藏条码不能超过 50 个字符")
        String barcode) {
}

