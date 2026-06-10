package com.humber.library.reader;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/readers")
public class ReaderController {
    private final ReaderRepository readerRepository;
    private final ReaderService readerService;

    public ReaderController(ReaderRepository readerRepository, ReaderService readerService) {
        this.readerRepository = readerRepository;
        this.readerService = readerService;
    }

    @GetMapping
    public List<ReaderSummary> search(@RequestParam(defaultValue = "") String keyword) {
        return readerRepository.search(keyword);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReaderSummary create(@Valid @RequestBody ReaderCreateRequest request) {
        return readerService.create(request);
    }

    @PutMapping("/{readerId}")
    public ReaderSummary update(
            @PathVariable long readerId,
            @Valid @RequestBody ReaderCreateRequest request) {
        return readerService.update(readerId, request);
    }

    @PutMapping("/{readerId}/status")
    public ReaderSummary updateStatus(
            @PathVariable long readerId,
            @Valid @RequestBody ReaderStatusUpdateRequest request) {
        return readerService.updateStatus(readerId, request);
    }
}
