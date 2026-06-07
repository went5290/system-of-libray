package com.humber.library.borrow;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class BorrowRepository {
    private final JdbcClient jdbcClient;

    public BorrowRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<BorrowRecordSummary> search(String keyword) {
        String normalized = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return jdbcClient.sql("""
                select br.id, r.reader_no, bc.barcode, b.title,
                       br.borrowed_at, br.due_at, br.returned_at, br.renew_count, br.status,
                       fr.id fine_id,
                       nvl(fr.amount, 0) fine_amount,
                       nvl(fr.paid_amount, 0) paid_amount,
                       fr.status fine_status
                  from borrow_record br
                  join reader r on r.id = br.reader_id
                  join book_copy bc on bc.id = br.book_copy_id
                  join book b on b.id = bc.book_id
                  left join fine_record fr on fr.borrow_record_id = br.id
                 where lower(r.reader_no) like :keyword
                    or lower(bc.barcode) like :keyword
                    or lower(b.title) like :keyword
                 order by br.borrowed_at desc
                """)
                .param("keyword", normalized)
                .query((rs, rowNum) -> new BorrowRecordSummary(
                        rs.getLong("id"),
                        rs.getString("reader_no"),
                        rs.getString("barcode"),
                        rs.getString("title"),
                        rs.getTimestamp("borrowed_at").toLocalDateTime(),
                        rs.getTimestamp("due_at").toLocalDateTime(),
                        rs.getTimestamp("returned_at") == null ? null : rs.getTimestamp("returned_at").toLocalDateTime(),
                        rs.getInt("renew_count"),
                        rs.getString("status"),
                        rs.getObject("fine_id") == null ? null : rs.getLong("fine_id"),
                        rs.getBigDecimal("fine_amount"),
                        rs.getBigDecimal("paid_amount"),
                        rs.getString("fine_status")))
                .list();
    }

    public Optional<ReaderBorrowState> lockReader(String readerNo) {
        return jdbcClient.sql("""
                select id, reader_no, status, max_borrow_count
                  from reader
                 where reader_no = :readerNo
                   for update
                """)
                .param("readerNo", readerNo)
                .query((rs, rowNum) -> new ReaderBorrowState(
                        rs.getLong("id"),
                        rs.getString("reader_no"),
                        rs.getString("status"),
                        rs.getInt("max_borrow_count")))
                .optional();
    }

    public Optional<BookCopyBorrowState> lockBookCopy(String barcode) {
        return jdbcClient.sql("""
                select bc.id, bc.barcode, bc.status, b.title
                  from book_copy bc
                  join book b on b.id = bc.book_id
                 where bc.barcode = :barcode
                   for update
                """)
                .param("barcode", barcode)
                .query((rs, rowNum) -> new BookCopyBorrowState(
                        rs.getLong("id"),
                        rs.getString("barcode"),
                        rs.getString("status"),
                        rs.getString("title")))
                .optional();
    }

    public int activeBorrowCount(long readerId) {
        return jdbcClient.sql("""
                select count(*)
                  from borrow_record
                 where reader_id = :readerId
                   and status in ('BORROWED', 'OVERDUE')
                """)
                .param("readerId", readerId)
                .query(Integer.class)
                .single();
    }

    public int borrowDays() {
        return jdbcClient.sql("select to_number(rule_value) from library_rule where rule_code = 'BORROW_DAYS'")
                .query(Integer.class)
                .single();
    }

    public int maxRenewCount() {
        return jdbcClient.sql("select to_number(rule_value) from library_rule where rule_code = 'MAX_RENEW_COUNT'")
                .query(Integer.class)
                .single();
    }

    public BigDecimal finePerDay() {
        return jdbcClient.sql("select to_number(rule_value) from library_rule where rule_code = 'FINE_PER_DAY'")
                .query(BigDecimal.class)
                .single();
    }

    public long nextId() {
        return jdbcClient.sql("select seq_borrow_record.nextval from dual")
                .query(Long.class)
                .single();
    }

    public long nextFineId() {
        return jdbcClient.sql("select seq_fine_record.nextval from dual")
                .query(Long.class)
                .single();
    }

    public void insert(long id, long readerId, long bookCopyId, LocalDateTime borrowedAt, LocalDateTime dueAt) {
        jdbcClient.sql("""
                insert into borrow_record (id, reader_id, book_copy_id, borrowed_at, due_at, status)
                values (:id, :readerId, :bookCopyId, :borrowedAt, :dueAt, 'BORROWED')
                """)
                .param("id", id)
                .param("readerId", readerId)
                .param("bookCopyId", bookCopyId)
                .param("borrowedAt", borrowedAt)
                .param("dueAt", dueAt)
                .update();
    }

    public void markCopyBorrowed(long bookCopyId) {
        jdbcClient.sql("update book_copy set status = 'BORROWED' where id = :id")
                .param("id", bookCopyId)
                .update();
    }

    public Optional<ActiveBorrowState> lockActiveBorrow(String barcode) {
        return jdbcClient.sql("""
                select br.id, br.book_copy_id, br.due_at, r.reader_no, bc.barcode, b.title
                  from borrow_record br
                  join reader r on r.id = br.reader_id
                  join book_copy bc on bc.id = br.book_copy_id
                  join book b on b.id = bc.book_id
                 where bc.barcode = :barcode
                   and br.status in ('BORROWED', 'OVERDUE')
                   for update
                """)
                .param("barcode", barcode)
                .query((rs, rowNum) -> new ActiveBorrowState(
                        rs.getLong("id"),
                        rs.getLong("book_copy_id"),
                        rs.getTimestamp("due_at").toLocalDateTime(),
                        rs.getString("reader_no"),
                        rs.getString("barcode"),
                        rs.getString("title")))
                .optional();
    }

    public void markBorrowReturned(long borrowId, LocalDateTime returnedAt) {
        jdbcClient.sql("""
                update borrow_record
                   set returned_at = :returnedAt,
                       status = 'RETURNED'
                 where id = :id
                """)
                .param("returnedAt", returnedAt)
                .param("id", borrowId)
                .update();
    }

    public void markCopyAvailable(long bookCopyId) {
        jdbcClient.sql("update book_copy set status = 'AVAILABLE' where id = :id")
                .param("id", bookCopyId)
                .update();
    }

    public void insertFine(long id, long borrowId, BigDecimal amount) {
        jdbcClient.sql("""
                insert into fine_record (id, borrow_record_id, amount, paid_amount, status)
                values (:id, :borrowId, :amount, 0, 'UNPAID')
                """)
                .param("id", id)
                .param("borrowId", borrowId)
                .param("amount", amount)
                .update();
    }

    public Optional<RenewBorrowState> lockBorrowForRenew(long borrowId) {
        return jdbcClient.sql("""
                select br.id, br.due_at, br.renew_count, br.status,
                       r.reader_no, bc.barcode, b.title
                  from borrow_record br
                  join reader r on r.id = br.reader_id
                  join book_copy bc on bc.id = br.book_copy_id
                  join book b on b.id = bc.book_id
                 where br.id = :borrowId
                   for update
                """)
                .param("borrowId", borrowId)
                .query((rs, rowNum) -> new RenewBorrowState(
                        rs.getLong("id"),
                        rs.getTimestamp("due_at").toLocalDateTime(),
                        rs.getInt("renew_count"),
                        rs.getString("status"),
                        rs.getString("reader_no"),
                        rs.getString("barcode"),
                        rs.getString("title")))
                .optional();
    }

    public void renew(long borrowId, LocalDateTime dueAt, int renewCount) {
        jdbcClient.sql("""
                update borrow_record
                   set due_at = :dueAt,
                       renew_count = :renewCount
                 where id = :id
                """)
                .param("dueAt", dueAt)
                .param("renewCount", renewCount)
                .param("id", borrowId)
                .update();
    }

    public record ReaderBorrowState(long id, String readerNo, String status, int maxBorrowCount) {
    }

    public record BookCopyBorrowState(long id, String barcode, String status, String bookTitle) {
    }

    public record ActiveBorrowState(
            long id,
            long bookCopyId,
            LocalDateTime dueAt,
            String readerNo,
            String barcode,
            String bookTitle) {
    }

    public record RenewBorrowState(
            long id,
            LocalDateTime dueAt,
            int renewCount,
            String status,
            String readerNo,
            String barcode,
            String bookTitle) {
    }
}
