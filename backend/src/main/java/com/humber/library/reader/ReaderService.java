package com.humber.library.reader;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReaderService {
    private final ReaderRepository readerRepository;

    public ReaderService(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }

    @Transactional
    public ReaderSummary create(ReaderCreateRequest request) {
        String readerNo = request.readerNo().trim();
        if (readerRepository.readerNoExists(readerNo)) {
            throw new IllegalArgumentException("借书证号已存在");
        }

        int maxBorrowCount = request.maxBorrowCount() == null ? 5 : request.maxBorrowCount();
        long id = readerRepository.nextId();
        readerRepository.insert(id, readerNo, request, maxBorrowCount);
        return readerRepository.search(readerNo).getFirst();
    }

    @Transactional
    public ReaderSummary updateStatus(long readerId, ReaderStatusUpdateRequest request) {
        ReaderSummary reader = readerRepository.lockById(readerId)
                .orElseThrow(() -> new IllegalArgumentException("读者不存在"));

        String status = request.status().name();
        if ("CLOSED".equals(status) && readerRepository.activeBorrowCount(reader.id()) > 0) {
            throw new IllegalArgumentException("读者存在未归还图书，不能注销");
        }

        readerRepository.updateStatus(reader.id(), status);
        return new ReaderSummary(
                reader.id(),
                reader.readerNo(),
                reader.phone(),
                reader.email(),
                reader.maxBorrowCount(),
                status,
                reader.createdAt());
    }
}
