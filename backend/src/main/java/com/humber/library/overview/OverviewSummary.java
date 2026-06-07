package com.humber.library.overview;

import java.math.BigDecimal;

public record OverviewSummary(
        int totalBooks,
        int totalCopies,
        int availableCopies,
        int activeReaders,
        int activeBorrows,
        int overdueBorrows,
        BigDecimal unpaidFineAmount) {
}
