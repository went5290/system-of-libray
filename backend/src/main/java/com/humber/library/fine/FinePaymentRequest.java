package com.humber.library.fine;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FinePaymentRequest(
        @NotNull(message = "缴费金额不能为空")
        @DecimalMin(value = "0.01", message = "缴费金额必须大于 0")
        @Digits(integer = 8, fraction = 2, message = "缴费金额最多保留两位小数")
        BigDecimal amount) {
}

