package com.humber.library.rule;

import java.math.BigDecimal;

public record LibraryRuleSettings(
        int borrowDays,
        int maxRenewCount,
        BigDecimal finePerDay) {
}
