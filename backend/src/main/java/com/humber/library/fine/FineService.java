package com.humber.library.fine;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FineService {
    private final FineRepository fineRepository;

    public FineService(FineRepository fineRepository) {
        this.fineRepository = fineRepository;
    }

    @Transactional
    public FinePaymentResponse pay(long fineId, FinePaymentRequest request) {
        FineRepository.FineState fine = fineRepository.lockFine(fineId)
                .orElseThrow(() -> new IllegalArgumentException("罚款记录不存在"));

        if ("PAID".equals(fine.status()) || "WAIVED".equals(fine.status())) {
            throw new IllegalArgumentException("当前罚款状态不可缴费");
        }

        BigDecimal payment = request.amount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingBeforePayment = fine.amount().subtract(fine.paidAmount());
        if (payment.compareTo(remainingBeforePayment) > 0) {
            throw new IllegalArgumentException("缴费金额不能超过剩余未缴金额");
        }

        BigDecimal paidAmount = fine.paidAmount().add(payment);
        BigDecimal remainingAmount = fine.amount().subtract(paidAmount);
        boolean paidInFull = remainingAmount.signum() == 0;
        String status = paidInFull ? "PAID" : "PARTIAL";
        LocalDateTime paidAt = paidInFull ? LocalDateTime.now() : null;

        fineRepository.updatePayment(fine.id(), paidAmount, status, paidAt);
        return new FinePaymentResponse(
                fine.id(),
                fine.borrowRecordId(),
                fine.amount(),
                paidAmount,
                remainingAmount,
                status,
                paidAt);
    }
}

