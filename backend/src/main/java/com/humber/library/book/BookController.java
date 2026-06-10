package com.humber.library.book;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public BookCategorySummary createCategory(@Valid @RequestBody BookCategoryRequest request) {
        return bookService.createCategory(request);
    }

    @PutMapping("/categories/{categoryId}")
    public BookCategorySummary updateCategory(
            @PathVariable long categoryId,
            @Valid @RequestBody BookCategoryRequest request) {
        return bookService.updateCategory(categoryId, request);
    }

    @GetMapping("/{bookId}/copies")
    public List<BookCopySummary> copies(@PathVariable long bookId) {
        if (!bookRepository.bookExists(bookId)) {
            throw new IllegalArgumentException("书目不存在");
        }
        return bookRepository.findCopies(bookId);
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

    @PutMapping("/{bookId}")
    public BookUpdateResponse update(@PathVariable long bookId, @Valid @RequestBody BookCreateRequest request) {
        return bookService.update(bookId, request);
    }

    @PutMapping("/copies/{copyId}/status")
    public BookCopySummary updateCopyStatus(
            @PathVariable long copyId,
            @Valid @RequestBody BookCopyStatusUpdateRequest request) {
        return bookService.updateCopyStatus(copyId, request);
    }

    @PutMapping("/copies/{copyId}/location")
    public BookCopySummary updateCopyLocation(
            @PathVariable long copyId,
            @Valid @RequestBody BookCopyLocationUpdateRequest request) {
        return bookService.updateCopyLocation(copyId, request);
    }
}
