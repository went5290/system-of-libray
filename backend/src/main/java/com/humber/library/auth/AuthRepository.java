package com.humber.library.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {
    private final JdbcClient jdbcClient;

    public AuthRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<AuthUser> findByUsername(String username) {
        return jdbcClient.sql("""
                select id, username, password_hash, display_name, enabled
                  from sys_user
                 where lower(username) = lower(:username)
                """)
                .param("username", username)
                .query((rs, rowNum) -> new AuthUser(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("display_name"),
                        rs.getInt("enabled") == 1))
                .optional();
    }

    public List<String> findRoleCodes(long userId) {
        return jdbcClient.sql("""
                select r.role_code
                  from sys_role r
                  join sys_user_role ur on ur.role_id = r.id
                 where ur.user_id = :userId
                 order by r.role_code
                """)
                .param("userId", userId)
                .query(String.class)
                .list();
    }
}
