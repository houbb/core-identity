package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.AttributeMapping;

import java.util.List;

/**
 * Repository for identity_attribute_mapping.
 */
public interface AttributeMappingRepository {
    void save(AttributeMapping mapping);
    List<AttributeMapping> findByConnectionId(String connectionId);
    void deleteById(String id);
    void update(AttributeMapping mapping);
}
