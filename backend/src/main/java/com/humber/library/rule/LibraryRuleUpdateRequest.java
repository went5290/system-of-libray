package com.humber.library.rule;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LibraryRuleUpdateRequest(
        @NotNull(message = "默认借阅天数不能为空")
        @Min(value = 1, message = "默认借阅天数不能少于 1 天")
        @Max(value = 365, message = "默认借阅天数不能超过 365 天")
        Integer borrowDays,

        @NotNull(message = "最大续借次数不能为空")
        @Min(value = 0, message = "最大续借次数不能小于 0")
        @Max(value = 20, message = "最大续借次数不能超过 20")
        Integer maxRenewCount,

        @NotNull(message = "每日逾期费用不能为空")
        @DecimalMin(value = "0.00", message = "每日逾期费用不能小于 0")
        @DecimalMax(value = "999.99", message = "每日逾期费用不能超过 999.99")
        BigDecimal finePerDay) {
}
