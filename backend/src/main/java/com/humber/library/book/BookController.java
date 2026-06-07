package com.humber.library.book;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookRepository bookRepository;
    private final BookService bookService;

    public BookController(BookRepository bookRepository, BookService bookService) {
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookSummary> search(@RequestParam(defaultValue = "") String keyword) {
        return bookRepository.search(keyword);
    }

    @GetMapping("/categories")
    public List<BookCategorySummary> categories() {
        return bookRepository.findCategories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookCreateResponse create(@Valid @RequestBody BookCreateRequest request) {
        return bookService.create(request);
    }

    @PostMapping("/{bookId}/copies")
    @ResponseStatus(HttpStatus.CREATED)
    public BookCopyCreateResponse createCopy(
            @PathVariable long bookId,
            @Valid @RequestBody BookCopyCreateRequest request) {
        return bookService.createCopy(bookId, request);
    }
}
