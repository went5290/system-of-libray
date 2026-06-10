package com.humber.library.operationlog;

import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class OperationLogRepository {
    private final JdbcClient jdbcClient;

    public OperationLogRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void insert(long userId, String action, String targetType, String targetId, String detail, String ipAddress) {
        jdbcClient.sql("""
                insert into operation_log (id, user_id, action, target_type, target_id, detail, ip_address)
                values (seq_operation_log.nextval, :userId, :action, :targetType, :targetId, :detail, :ipAddress)
                """)
                .param("userId", userId)
                .param("action", action)
                .param("targetType", targetType)
                .param("targetId", targetId)
                .param("detail", detail)
                .param("ipAddress", ipAddress)
                .update();
    }

    public List<OperationLogSummary> search(String keyword) {
        String normalized = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return jdbcClient.sql("""
                select l.id, u.username, u.display_name, l.action, l.target_type, l.target_id,
                       l.detail, l.ip_address, l.created_at
                  from operation_log l
                  left join sys_user u on u.id = l.user_id
                 where lower(l.action) like :keyword
                    or lower(nvl(l.target_type, '')) like :keyword
                    or lower(nvl(l.target_id, '')) like :keyword
                    or lower(nvl(l.detail, '')) like :keyword
                    or lower(nvl(u.username, '')) like :keyword
                    or lower(nvl(u.display_name, '')) like :keyword
                 order by l.created_at desc
                 fetch first 200 rows only
                """)
                .param("keyword", normalized)
                .query((rs, rowNum) -> new OperationLogSummary(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("display_name"),
                        rs.getString("action"),
                        rs.getString("target_type"),
                        rs.getString("target_id"),
                        rs.getString("detail"),
                        rs.getString("ip_address"),
                        rs.getTimestamp("created_at").toLocalDateTime()))
                .list();
    }
}
