package com.humber.library.rule;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rules")
public class LibraryRuleController {
    private final LibraryRuleRepository libraryRuleRepository;
    private final LibraryRuleService libraryRuleService;

    public LibraryRuleController(
            LibraryRuleRepository libraryRuleRepository,
            LibraryRuleService libraryRuleService) {
        this.libraryRuleRepository = libraryRuleRepository;
        this.libraryRuleService = libraryRuleService;
    }

    @GetMapping
    public LibraryRuleSettings getSettings() {
        return libraryRuleRepository.getSettings();
    }

    @PutMapping
    public LibraryRuleSettings update(@Valid @RequestBody LibraryRuleUpdateRequest request) {
        return libraryRuleService.update(request);
    }
}
