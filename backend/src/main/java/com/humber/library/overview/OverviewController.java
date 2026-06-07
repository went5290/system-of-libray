package com.humber.library.overview;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/overview")
public class OverviewController {
    private final OverviewRepository overviewRepository;

    public OverviewController(OverviewRepository overviewRepository) {
        this.overviewRepository = overviewRepository;
    }

    @GetMapping
    public OverviewSummary summary() {
        return overviewRepository.summary();
    }
}
