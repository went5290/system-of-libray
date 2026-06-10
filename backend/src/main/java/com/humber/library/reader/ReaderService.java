package com.humber.library.reader;

import com.humber.library.operationlog.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final OperationLogService operationLogService;

    public ReaderService(ReaderRepository readerRepository, OperationLogService operationLogService) {
        this.readerRepository = readerRepository;
        this.operationLogService = operationLogService;
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
        operationLogService.record("CREATE_READER", "READER", id, "新增读者：" + readerNo);
        return readerRepository.search(readerNo).getFirst();
    }

    @Transactional
    public ReaderSummary update(long readerId, ReaderCreateRequest request) {
        ReaderSummary reader = readerRepository.lockById(readerId)
                .orElseThrow(() -> new IllegalArgumentException("读者不存在"));

        String readerNo = request.readerNo().trim();
        if (readerRepository.readerNoExistsForOtherReader(readerNo, reader.id())) {
            throw new IllegalArgumentException("借书证号已存在");
        }

        int maxBorrowCount = request.maxBorrowCount() == null
                ? reader.maxBorrowCount()
                : request.maxBorrowCount();
        readerRepository.update(reader.id(), readerNo, request, maxBorrowCount);
        operationLogService.record("UPDATE_READER", "READER", reader.id(), "编辑读者：" + readerNo);
        return new ReaderSummary(
                reader.id(),
                readerNo,
                normalize(request.phone()),
                normalize(request.email()),
                maxBorrowCount,
                reader.status(),
                reader.createdAt());
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
        operationLogService.record(
                "UPDATE_READER_STATUS",
                "READER",
                reader.id(),
                "读者 " + reader.readerNo() + " 状态更新为 " + status);
        return new ReaderSummary(
                reader.id(),
                reader.readerNo(),
                reader.phone(),
                reader.email(),
                reader.maxBorrowCount(),
                status,
                reader.createdAt());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
