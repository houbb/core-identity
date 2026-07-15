package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.TotpAuthenticator;
import com.github.houbb.core.identity.application.port.TotpAuthenticatorRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcTotpAuthenticatorRepository implements TotpAuthenticatorRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTotpAuthenticatorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(TotpAuthenticator totp) {
        jdbcTemplate.update(
                "INSERT INTO identity_totp_authenticator (authenticator_id, encrypted_secret, " +
                "encryption_key_version, algorithm, digits, period_seconds, last_accepted_step, confirmed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                totp.getAuthenticatorId(), totp.getEncryptedSecret(), totp.getEncryptionKeyVersion(),
                totp.getAlgorithm(), totp.getDigits(), totp.getPeriodSeconds(),
                totp.getLastAcceptedStep(), totp.getConfirmedAt()
        );
    }

    @Override
    public Optional<TotpAuthenticator> findByAuthenticatorId(String authenticatorId) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_totp_authenticator WHERE authenticator_id = ?",
                    new TotpRowMapper(), authenticatorId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateLastAcceptedStep(String authenticatorId, long lastAcceptedStep, long confirmedAt) {
        jdbcTemplate.update(
                "UPDATE identity_totp_authenticator SET last_accepted_step = ?, confirmed_at = ? " +
                "WHERE authenticator_id = ?",
                lastAcceptedStep, confirmedAt, authenticatorId
        );
    }

    @Override
    public void deleteByAuthenticatorId(String authenticatorId) {
        jdbcTemplate.update(
                "DELETE FROM identity_totp_authenticator WHERE authenticator_id = ?", authenticatorId);
    }

    static class TotpRowMapper implements RowMapper<TotpAuthenticator> {
        @Override
        public TotpAuthenticator mapRow(ResultSet rs, int rowNum) throws SQLException {
            TotpAuthenticator t = new TotpAuthenticator();
            t.setAuthenticatorId(rs.getString("authenticator_id"));
            t.setEncryptedSecret(rs.getString("encrypted_secret"));
            t.setEncryptionKeyVersion(rs.getString("encryption_key_version"));
            t.setAlgorithm(rs.getString("algorithm"));
            t.setDigits(rs.getInt("digits"));
            t.setPeriodSeconds(rs.getInt("period_seconds"));
            t.setLastAcceptedStep(rs.getLong("last_accepted_step"));
            t.setConfirmedAt(JdbcUserRepository.getNullableLong(rs, "confirmed_at"));
            return t;
        }
    }
}
