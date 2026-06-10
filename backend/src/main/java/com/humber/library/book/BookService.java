package com.humber.library.book;

import com.humber.library.operationlog.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final OperationLogService operationLogService;

    public BookService(BookRepository bookRepository, OperationLogService operationLogService) {
        this.bookRepository = bookRepository;
        this.operationLogService = operationLogService;
    }

    @Transactional
    public BookCreateResponse create(BookCreateRequest request) {
        String isbn = request.isbn().trim();
        String title = request.title().trim();

        if (!bookRepository.categoryExists(request.categoryId())) {
            throw new IllegalArgumentException("图书分类不存在");
        }
        if (bookRepository.isbnExists(isbn)) {
            throw new IllegalArgumentException("ISBN 已存在");
        }

        long id = bookRepository.nextId();
        bookRepository.insert(id, request, isbn, title);
        operationLogService.record("CREATE_BOOK", "BOOK", id, "新增书目：" + title + "，ISBN：" + isbn);
        return new BookCreateResponse(id, isbn, title);
    }

    @Transactional
    public BookCopyCreateResponse createCopy(long bookId, BookCopyCreateRequest request) {
        String barcode = request.barcode().trim();

        if (!bookRepository.bookExists(bookId)) {
            throw new IllegalArgumentException("书目不存在");
        }
        if (bookRepository.barcodeExists(barcode)) {
            throw new IllegalArgumentException("馆藏条码已存在");
        }

        long id = bookRepository.nextCopyId();
        bookRepository.insertCopy(id, bookId, barcode, request.shelfLocation());
        operationLogService.record("CREATE_BOOK_COPY", "BOOK_COPY", id, "登记馆藏副本：" + barcode);
        return new BookCopyCreateResponse(id, bookId, barcode, "AVAILABLE");
    }
}
