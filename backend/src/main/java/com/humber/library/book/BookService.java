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
    public BookCategorySummary createCategory(BookCategoryRequest request) {
        String categoryName = request.categoryName().trim();
        if (bookRepository.categoryNameExists(categoryName)) {
            throw new IllegalArgumentException("分类名称已存在");
        }

        long id = bookRepository.nextCategoryId();
        bookRepository.insertCategory(id, categoryName, request.description());
        operationLogService.record("CREATE_BOOK_CATEGORY", "BOOK_CATEGORY", id, "新增图书分类：" + categoryName);
        return new BookCategorySummary(id, categoryName, normalize(request.description()));
    }

    @Transactional
    public BookCategorySummary updateCategory(long categoryId, BookCategoryRequest request) {
        if (!bookRepository.categoryExists(categoryId)) {
            throw new IllegalArgumentException("图书分类不存在");
        }

        String categoryName = request.categoryName().trim();
        if (bookRepository.categoryNameExistsForOtherCategory(categoryName, categoryId)) {
            throw new IllegalArgumentException("分类名称已存在");
        }

        bookRepository.updateCategory(categoryId, categoryName, request.description());
        operationLogService.record("UPDATE_BOOK_CATEGORY", "BOOK_CATEGORY", categoryId, "编辑图书分类：" + categoryName);
        return new BookCategorySummary(categoryId, categoryName, normalize(request.description()));
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

    @Transactional
    public BookUpdateResponse update(long bookId, BookCreateRequest request) {
        String isbn = request.isbn().trim();
        String title = request.title().trim();

        if (!bookRepository.bookExists(bookId)) {
            throw new IllegalArgumentException("书目不存在");
        }
        if (!bookRepository.categoryExists(request.categoryId())) {
            throw new IllegalArgumentException("图书分类不存在");
        }
        if (bookRepository.isbnExistsForOtherBook(isbn, bookId)) {
            throw new IllegalArgumentException("ISBN 已存在");
        }

        bookRepository.update(bookId, request, isbn, title);
        operationLogService.record("UPDATE_BOOK", "BOOK", bookId, "编辑书目：" + title + "，ISBN：" + isbn);
        return new BookUpdateResponse(bookId, isbn, title);
    }

    @Transactional
    public BookCopySummary updateCopyStatus(long copyId, BookCopyStatusUpdateRequest request) {
        BookCopySummary copy = bookRepository.lockCopyById(copyId)
                .orElseThrow(() -> new IllegalArgumentException("馆藏副本不存在"));
        if ("BORROWED".equals(copy.status())) {
            throw new IllegalArgumentException("正在借出的馆藏副本不能手工修改状态");
        }

        String status = request.status().name();
        bookRepository.updateCopyStatus(copy.id(), status);
        operationLogService.record(
                "UPDATE_BOOK_COPY_STATUS",
                "BOOK_COPY",
                copy.id(),
                "馆藏副本 " + copy.barcode() + " 状态更新为 " + status);
        return new BookCopySummary(
                copy.id(),
                copy.bookId(),
                copy.barcode(),
                copy.shelfLocation(),
                status,
                copy.acquiredAt());
    }

    @Transactional
    public BookCopySummary updateCopyLocation(long copyId, BookCopyLocationUpdateRequest request) {
        BookCopySummary copy = bookRepository.lockCopyById(copyId)
                .orElseThrow(() -> new IllegalArgumentException("馆藏副本不存在"));

        String shelfLocation = normalize(request.shelfLocation());
        bookRepository.updateCopyLocation(copy.id(), shelfLocation);
        operationLogService.record(
                "UPDATE_BOOK_COPY_LOCATION",
                "BOOK_COPY",
                copy.id(),
                "馆藏副本 " + copy.barcode() + " 书架位置更新为 " + (shelfLocation == null ? "未设置" : shelfLocation));
        return new BookCopySummary(
                copy.id(),
                copy.bookId(),
                copy.barcode(),
                shelfLocation,
                copy.status(),
                copy.acquiredAt());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
