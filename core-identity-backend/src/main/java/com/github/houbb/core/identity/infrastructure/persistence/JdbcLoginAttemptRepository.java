package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.LoginAttempt;
import com.github.houbb.core.identity.application.port.LoginAttemptRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcLoginAttemptRepository implements LoginAttemptRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcLoginAttemptRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(LoginAttempt attempt) {
        jdbcTemplate.update(
                "INSERT INTO identity_login_attempt (id, user_id, email_hash, result, failure_reason, " +
                "ip_address, user_agent, request_id, occurred_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                attempt.getId(), attempt.getUserId(), attempt.getEmailHash(), attempt.getResult(),
                attempt.getFailureReason(), attempt.getIpAddress(), attempt.getUserAgent(),
                attempt.getRequestId(), attempt.getOccurredAt()
        );
    }

    @Override
    public int countRecentFailuresByUser(String userId, long sinceTimestamp) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_login_attempt WHERE user_id = ? AND result = 'FAILURE' " +
                "AND occurred_at > ?", Integer.class, userId, sinceTimestamp);
        return count != null ? count : 0;
    }

    @Override
    public int countRecentAttemptsByIp(String ipAddress, long sinceTimestamp) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM identity_login_attempt WHERE ip_address = ? AND occurred_at > ?",
                Integer.class, ipAddress, sinceTimestamp);
        return count != null ? count : 0;
    }

    @Override
    public List<LoginAttempt> findByUserId(String userId, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM identity_login_attempt WHERE user_id = ? ORDER BY occurred_at DESC LIMIT ?",
                new LoginAttemptRowMapper(), userId, limit);
    }

    static class LoginAttemptRowMapper implements RowMapper<LoginAttempt> {
        @Override
        public LoginAttempt mapRow(ResultSet rs, int rowNum) throws SQLException {
            LoginAttempt a = new LoginAttempt();
            a.setId(rs.getString("id"));
            a.setUserId(rs.getString("user_id"));
            a.setEmailHash(rs.getString("email_hash"));
            a.setResult(rs.getString("result"));
            a.setFailureReason(rs.getString("failure_reason"));
            a.setIpAddress(rs.getString("ip_address"));
            a.setUserAgent(rs.getString("user_agent"));
            a.setRequestId(rs.getString("request_id"));
            a.setOccurredAt(rs.getLong("occurred_at"));
            return a;
        }
    }
}