package com.humber.library.borrow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowService {
    private final BorrowRepository borrowRepository;

    public BorrowService(BorrowRepository borrowRepository) {
        this.borrowRepository = borrowRepository;
    }

    @Transactional
    public BorrowCreateResponse borrow(BorrowCreateRequest request) {
        String readerNo = request.readerNo().trim();
        String barcode = request.barcode().trim();

        BorrowRepository.ReaderBorrowState reader = borrowRepository.lockReader(readerNo)
                .orElseThrow(() -> new IllegalArgumentException("读者不存在"));
        if (!"ACTIVE".equals(reader.status())) {
            throw new IllegalArgumentException("读者状态不可借阅");
        }
        if (borrowRepository.activeBorrowCount(reader.id()) >= reader.maxBorrowCount()) {
            throw new IllegalArgumentException("读者已达到最大借阅数量");
        }

        BorrowRepository.BookCopyBorrowState copy = borrowRepository.lockBookCopy(barcode)
                .orElseThrow(() -> new IllegalArgumentException("馆藏副本不存在"));
        if (!"AVAILABLE".equals(copy.status())) {
            throw new IllegalArgumentException("馆藏副本当前不可借");
        }

        LocalDateTime borrowedAt = LocalDateTime.now();
        LocalDateTime dueAt = borrowedAt.plusDays(borrowRepository.borrowDays());
        long id = borrowRepository.nextId();
        borrowRepository.insert(id, reader.id(), copy.id(), borrowedAt, dueAt);
        borrowRepository.markCopyBorrowed(copy.id());

        return new BorrowCreateResponse(
                id, reader.readerNo(), copy.barcode(), copy.bookTitle(), borrowedAt, dueAt, "BORROWED");
    }

    @Transactional
    public ReturnBookResponse returnBook(ReturnBookRequest request) {
        String barcode = request.barcode().trim();
        BorrowRepository.ActiveBorrowState borrow = borrowRepository.lockActiveBorrow(barcode)
                .orElseThrow(() -> new IllegalArgumentException("未找到该副本的在借记录"));

        LocalDateTime returnedAt = LocalDateTime.now();
        long overdueSeconds = Math.max(0, Duration.between(borrow.dueAt(), returnedAt).getSeconds());
        long overdueDays = overdueSeconds == 0 ? 0 : (overdueSeconds + 86_399) / 86_400;
        BigDecimal fineAmount = borrowRepository.finePerDay()
                .multiply(BigDecimal.valueOf(overdueDays))
                .setScale(2, RoundingMode.HALF_UP);

        borrowRepository.markBorrowReturned(borrow.id(), returnedAt);
        borrowRepository.markCopyAvailable(borrow.bookCopyId());
        if (fineAmount.signum() > 0) {
            borrowRepository.insertFine(borrowRepository.nextFineId(), borrow.id(), fineAmount);
        }

        return new ReturnBookResponse(
                borrow.id(),
                borrow.readerNo(),
                borrow.barcode(),
                borrow.bookTitle(),
                returnedAt,
                overdueDays,
                fineAmount,
                "RETURNED");
    }

    @Transactional
    public BorrowRenewResponse renew(long borrowId) {
        BorrowRepository.RenewBorrowState borrow = borrowRepository.lockBorrowForRenew(borrowId)
                .orElseThrow(() -> new IllegalArgumentException("借阅记录不存在"));

        if (!"BORROWED".equals(borrow.status())) {
            throw new IllegalArgumentException("当前借阅状态不可续借");
        }
        if (!borrow.dueAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("已逾期借阅不可续借");
        }
        if (borrow.renewCount() >= borrowRepository.maxRenewCount()) {
            throw new IllegalArgumentException("已达到最大续借次数");
        }

        LocalDateTime newDueAt = borrow.dueAt().plusDays(borrowRepository.borrowDays());
        int newRenewCount = borrow.renewCount() + 1;
        borrowRepository.renew(borrow.id(), newDueAt, newRenewCount);

        return new BorrowRenewResponse(
                borrow.id(),
                borrow.readerNo(),
                borrow.barcode(),
                borrow.bookTitle(),
                newDueAt,
                newRenewCount,
                borrow.status());
    }
}
