package com.humber.library.overview;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class OverviewRepository {
    private final JdbcClient jdbcClient;

    public OverviewRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public OverviewSummary summary() {
        return jdbcClient.sql("""
                select (select count(*) from book) total_books,
                       (select count(*) from book_copy) total_copies,
                       (select count(*) from book_copy where status = 'AVAILABLE') available_copies,
                       (select count(*) from reader where status = 'ACTIVE') active_readers,
                       (select count(*) from borrow_record where status in ('BORROWED', 'OVERDUE')) active_borrows,
                       (select count(*)
                          from borrow_record
                         where status in ('BORROWED', 'OVERDUE')
                           and due_at < current_timestamp) overdue_borrows,
                       (select nvl(sum(amount - paid_amount), 0)
                          from fine_record
                         where status in ('UNPAID', 'PARTIAL')) unpaid_fine_amount
                  from dual
                """)
                .query((rs, rowNum) -> new OverviewSummary(
                        rs.getInt("total_books"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies"),
                        rs.getInt("active_readers"),
                        rs.getInt("active_borrows"),
                        rs.getInt("overdue_borrows"),
                        rs.getBigDecimal("unpaid_fine_amount")))
                .single();
    }
}
