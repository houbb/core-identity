package com.github.houbb.core.identity.application.port;

import com.github.houbb.core.identity.application.domain.ServiceAccount;
import java.util.List;
import java.util.Optional;

public interface ServiceAccountRepository {
    void save(ServiceAccount sa);
    Optional<ServiceAccount> findById(String id);
    List<ServiceAccount> findByOrg(String orgId);
    void update(ServiceAccount sa);
}
