package com.humber.library.operationlog;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operation-logs")
public class OperationLogController {
    private final OperationLogRepository operationLogRepository;

    public OperationLogController(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    @GetMapping
    public List<OperationLogSummary> search(@RequestParam(defaultValue = "") String keyword) {
        return operationLogRepository.search(keyword);
    }
}
