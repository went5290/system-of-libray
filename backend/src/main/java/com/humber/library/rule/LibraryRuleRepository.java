package com.humber.library.rule;

import java.math.BigDecimal;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LibraryRuleRepository {
    private final JdbcClient jdbcClient;

    public LibraryRuleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public LibraryRuleSettings getSettings() {
        return new LibraryRuleSettings(
                integerValue("BORROW_DAYS"),
                integerValue("MAX_RENEW_COUNT"),
                decimalValue("FINE_PER_DAY"));
    }

    public void updateSettings(LibraryRuleUpdateRequest request) {
        updateValue("BORROW_DAYS", request.borrowDays().toString());
        updateValue("MAX_RENEW_COUNT", request.maxRenewCount().toString());
        updateValue("FINE_PER_DAY", request.finePerDay().toPlainString());
    }

    private int integerValue(String ruleCode) {
        return jdbcClient.sql("select to_number(rule_value) from library_rule where rule_code = :ruleCode")
                .param("ruleCode", ruleCode)
                .query(Integer.class)
                .single();
    }

    private BigDecimal decimalValue(String ruleCode) {
        return jdbcClient.sql("select to_number(rule_value) from library_rule where rule_code = :ruleCode")
                .param("ruleCode", ruleCode)
                .query(BigDecimal.class)
                .single();
    }

    private void updateValue(String ruleCode, String ruleValue) {
        jdbcClient.sql("""
                update library_rule
                   set rule_value = :ruleValue,
                       updated_at = current_timestamp
                 where rule_code = :ruleCode
                """)
                .param("ruleValue", ruleValue)
                .param("ruleCode", ruleCode)
                .update();
    }
}
