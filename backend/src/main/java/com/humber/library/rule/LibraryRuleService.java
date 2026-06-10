package com.humber.library.rule;

import com.humber.library.operationlog.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryRuleService {
    private final LibraryRuleRepository libraryRuleRepository;
    private final OperationLogService operationLogService;

    public LibraryRuleService(
            LibraryRuleRepository libraryRuleRepository,
            OperationLogService operationLogService) {
        this.libraryRuleRepository = libraryRuleRepository;
        this.operationLogService = operationLogService;
    }

    @Transactional
    public LibraryRuleSettings update(LibraryRuleUpdateRequest request) {
        libraryRuleRepository.updateSettings(request);
        operationLogService.record(
                "UPDATE_LIBRARY_RULES",
                "LIBRARY_RULE",
                0,
                "更新借阅规则：借阅 " + request.borrowDays()
                        + " 天，最多续借 " + request.maxRenewCount()
                        + " 次，每日逾期费用 " + request.finePerDay());
        return libraryRuleRepository.getSettings();
    }
}
