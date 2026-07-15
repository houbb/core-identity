package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.Device;

import java.util.Optional;

/**
 * Repository for identity_device.
 */
public interface DeviceRepository {

    void save(Device device);

    Optional<Device> findById(String id);

    Optional<Device> findByUserIdAndCookieHash(String userId, String deviceCookieHash);

    void update(Device device);
}
