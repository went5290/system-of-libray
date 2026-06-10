package com.humber.library.reader;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ReaderRepository {
    private final JdbcClient jdbcClient;

    public ReaderRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ReaderSummary> search(String keyword) {
        String normalized = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return jdbcClient.sql("""
                select id, reader_no, phone, email, max_borrow_count, status, created_at
                  from reader
                 where lower(reader_no) like :keyword
                    or lower(nvl(phone, '')) like :keyword
                    or lower(nvl(email, '')) like :keyword
                 order by created_at desc
                """)
                .param("keyword", normalized)
                .query((rs, rowNum) -> new ReaderSummary(
                        rs.getLong("id"),
                        rs.getString("reader_no"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getInt("max_borrow_count"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime()))
                .list();
    }

    public boolean readerNoExists(String readerNo) {
        return jdbcClient.sql("select count(*) from reader where reader_no = :readerNo")
                .param("readerNo", readerNo)
                .query(Integer.class)
                .single() > 0;
    }

    public boolean readerNoExistsForOtherReader(String readerNo, long readerId) {
        return jdbcClient.sql("""
                select count(*)
                  from reader
                 where reader_no = :readerNo
                   and id <> :readerId
                """)
                .param("readerNo", readerNo)
                .param("readerId", readerId)
                .query(Integer.class)
                .single() > 0;
    }

    public Optional<ReaderSummary> lockById(long id) {
        return jdbcClient.sql("""
                select id, reader_no, phone, email, max_borrow_count, status, created_at
                  from reader
                 where id = :id
                   for update
                """)
                .param("id", id)
                .query((rs, rowNum) -> new ReaderSummary(
                        rs.getLong("id"),
                        rs.getString("reader_no"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getInt("max_borrow_count"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime()))
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

    public long nextId() {
        return jdbcClient.sql("select seq_reader.nextval from dual")
                .query(Long.class)
                .single();
    }

    public void insert(long id, String readerNo, ReaderCreateRequest request, int maxBorrowCount) {
        jdbcClient.sql("""
                insert into reader (id, reader_no, phone, email, max_borrow_count, status)
                values (:id, :readerNo, :phone, :email, :maxBorrowCount, 'ACTIVE')
                """)
                .param("id", id)
                .param("readerNo", readerNo)
                .param("phone", trimToNull(request.phone()))
                .param("email", trimToNull(request.email()))
                .param("maxBorrowCount", maxBorrowCount)
                .update();
    }

    public void updateStatus(long id, String status) {
        jdbcClient.sql("update reader set status = :status where id = :id")
                .param("status", status)
                .param("id", id)
                .update();
    }

    public void update(long id, String readerNo, ReaderCreateRequest request, int maxBorrowCount) {
        jdbcClient.sql("""
                update reader
                   set reader_no = :readerNo,
                       phone = :phone,
                       email = :email,
                       max_borrow_count = :maxBorrowCount
                 where id = :id
                """)
                .param("readerNo", readerNo)
                .param("phone", trimToNull(request.phone()))
                .param("email", trimToNull(request.email()))
                .param("maxBorrowCount", maxBorrowCount)
                .param("id", id)
                .update();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
