package com.github.houbb.core.identity.infrastructure.persistence;

import com.github.houbb.core.identity.application.domain.Device;
import com.github.houbb.core.identity.application.port.DeviceRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcDeviceRepository implements DeviceRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcDeviceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Device d) {
        jdbcTemplate.update(
                "INSERT INTO identity_device (id, user_id, device_cookie_hash, display_name, browser, " +
                "operating_system, first_seen_at, last_seen_at, last_ip, status, created_at, updated_at, version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                d.getId(), d.getUserId(), d.getDeviceCookieHash(), d.getDisplayName(), d.getBrowser(),
                d.getOperatingSystem(), d.getFirstSeenAt(), d.getLastSeenAt(), d.getLastIp(),
                d.getStatus(), d.getCreatedAt(), d.getUpdatedAt(), d.getVersion()
        );
    }

    @Override
    public Optional<Device> findById(String id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_device WHERE id = ?", new DeviceRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Device> findByUserIdAndCookieHash(String userId, String deviceCookieHash) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM identity_device WHERE user_id = ? AND device_cookie_hash = ?",
                    new DeviceRowMapper(), userId, deviceCookieHash));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(Device d) {
        jdbcTemplate.update(
                "UPDATE identity_device SET display_name = ?, browser = ?, operating_system = ?, " +
                "last_seen_at = ?, last_ip = ?, status = ?, updated_at = ?, version = version + 1 " +
                "WHERE id = ? AND version = ?",
                d.getDisplayName(), d.getBrowser(), d.getOperatingSystem(),
                d.getLastSeenAt(), d.getLastIp(), d.getStatus(), d.getUpdatedAt(),
                d.getId(), d.getVersion()
        );
    }

    static class DeviceRowMapper implements RowMapper<Device> {
        @Override
        public Device mapRow(ResultSet rs, int rowNum) throws SQLException {
            Device d = new Device();
            d.setId(rs.getString("id"));
            d.setUserId(rs.getString("user_id"));
            d.setDeviceCookieHash(rs.getString("device_cookie_hash"));
            d.setDisplayName(rs.getString("display_name"));
            d.setBrowser(rs.getString("browser"));
            d.setOperatingSystem(rs.getString("operating_system"));
            d.setFirstSeenAt(rs.getLong("first_seen_at"));
            d.setLastSeenAt(rs.getLong("last_seen_at"));
            d.setLastIp(rs.getString("last_ip"));
            d.setStatus(rs.getString("status"));
            d.setCreatedAt(rs.getLong("created_at"));
            d.setUpdatedAt(rs.getLong("updated_at"));
            d.setVersion(rs.getLong("version"));
            return d;
        }
    }
}
