package com.humber.library.fine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class FineRepository {
    private final JdbcClient jdbcClient;

    public FineRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<FineState> lockFine(long fineId) {
        return jdbcClient.sql("""
                select id, borrow_record_id, amount, paid_amount, status, paid_at
                  from fine_record
                 where id = :fineId
                   for update
                """)
                .param("fineId", fineId)
                .query((rs, rowNum) -> new FineState(
                        rs.getLong("id"),
                        rs.getLong("borrow_record_id"),
                        rs.getBigDecimal("amount"),
                        rs.getBigDecimal("paid_amount"),
                        rs.getString("status"),
                        rs.getTimestamp("paid_at") == null ? null : rs.getTimestamp("paid_at").toLocalDateTime()))
                .optional();
    }

    public void updatePayment(long fineId, BigDecimal paidAmount, String status, LocalDateTime paidAt) {
        jdbcClient.sql("""
                update fine_record
                   set paid_amount = :paidAmount,
                       status = :status,
                       paid_at = :paidAt
                 where id = :fineId
                """)
                .param("paidAmount", paidAmount)
                .param("status", status)
                .param("paidAt", paidAt)
                .param("fineId", fineId)
                .update();
    }

    public record FineState(
            long id,
            long borrowRecordId,
            BigDecimal amount,
            BigDecimal paidAmount,
            String status,
            LocalDateTime paidAt) {
    }
}

