package com.humber.library.borrow;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/returns")
public class ReturnController {
    private final BorrowService borrowService;

    public ReturnController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @PostMapping
    public ReturnBookResponse returnBook(@Valid @RequestBody ReturnBookRequest request) {
        return borrowService.returnBook(request);
    }
}
