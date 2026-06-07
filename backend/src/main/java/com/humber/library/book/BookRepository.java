package com.humber.library.book;

import java.util.List;
import java.sql.Date;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {
    private final JdbcClient jdbcClient;

    public BookRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<BookSummary> search(String keyword) {
        String normalized = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return jdbcClient.sql("""
                select b.id, b.isbn, b.title, b.author, b.publisher, c.category_name,
                       count(bc.id) total_copies,
                       sum(case when bc.status = 'AVAILABLE' then 1 else 0 end) available_copies
                  from book b
                  left join book_category c on c.id = b.category_id
                  left join book_copy bc on bc.book_id = b.id
                 where lower(b.title) like :keyword
                    or lower(nvl(b.author, '')) like :keyword
                    or lower(nvl(b.isbn, '')) like :keyword
                 group by b.id, b.isbn, b.title, b.author, b.publisher, c.category_name
                 order by b.title
                """)
                .param("keyword", normalized)
                .query((rs, rowNum) -> new BookSummary(
                        rs.getLong("id"),
                        rs.getString("isbn"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("publisher"),
                        rs.getString("category_name"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")))
                .list();
    }

    public List<BookCategorySummary> findCategories() {
        return jdbcClient.sql("select id, category_name from book_category order by category_name")
                .query((rs, rowNum) -> new BookCategorySummary(
                        rs.getLong("id"),
                        rs.getString("category_name")))
                .list();
    }

    public boolean categoryExists(long categoryId) {
        return jdbcClient.sql("select count(*) from book_category where id = :id")
                .param("id", categoryId)
                .query(Integer.class)
                .single() > 0;
    }

    public boolean bookExists(long bookId) {
        return jdbcClient.sql("select count(*) from book where id = :id")
                .param("id", bookId)
                .query(Integer.class)
                .single() > 0;
    }

    public boolean barcodeExists(String barcode) {
        return jdbcClient.sql("select count(*) from book_copy where barcode = :barcode")
                .param("barcode", barcode)
                .query(Integer.class)
                .single() > 0;
    }

    public boolean isbnExists(String isbn) {
        return jdbcClient.sql("select count(*) from book where isbn = :isbn")
                .param("isbn", isbn)
                .query(Integer.class)
                .single() > 0;
    }

    public long nextId() {
        return jdbcClient.sql("select seq_book.nextval from dual")
                .query(Long.class)
                .single();
    }

    public long nextCopyId() {
        return jdbcClient.sql("select seq_book_copy.nextval from dual")
                .query(Long.class)
                .single();
    }

    public void insert(long id, BookCreateRequest request, String isbn, String title) {
        jdbcClient.sql("""
                insert into book (id, isbn, title, author, publisher, publish_date, category_id, description)
                values (:id, :isbn, :title, :author, :publisher, :publishDate, :categoryId, :description)
                """)
                .param("id", id)
                .param("isbn", isbn)
                .param("title", title)
                .param("author", trimToNull(request.author()))
                .param("publisher", trimToNull(request.publisher()))
                .param("publishDate", request.publishDate() == null ? null : Date.valueOf(request.publishDate()))
                .param("categoryId", request.categoryId())
                .param("description", trimToNull(request.description()))
                .update();
    }

    public void insertCopy(long id, long bookId, String barcode, String shelfLocation) {
        jdbcClient.sql("""
                insert into book_copy (id, book_id, barcode, shelf_location, status)
                values (:id, :bookId, :barcode, :shelfLocation, 'AVAILABLE')
                """)
                .param("id", id)
                .param("bookId", bookId)
                .param("barcode", barcode)
                .param("shelfLocation", trimToNull(shelfLocation))
                .update();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
