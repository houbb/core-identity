package com.github.houbb.core.identity.infrastructure.configuration;

import com.github.houbb.core.identity.application.port.*;
import com.github.houbb.core.identity.application.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application service bean configuration.
 */
@Configuration
public class ServiceConfiguration {

    @Value("${core.identity.instance-name:Core Identity}")
    private String instanceName;

    @Value("${core.identity.edition:COMMUNITY}")
    private String edition;

    @Value("${core.internal-auth.signing-key}")
    private String signingKey;

    @Value("${core.internal-auth.issuer:core-identity}")
    private String issuer;

    @Value("${core.internal-auth.token-ttl-seconds:600}")
    private int tokenTtlSeconds;

    @Value("${core.idempotency.record-ttl-hours:24}")
    private int recordTtlHours;

    @Bean
    public SystemInfoService systemInfoService() {
        return new SystemInfoServiceImpl("0.1.0", "v1", instanceName, edition);
    }

    @Bean
    public InternalTokenService internalTokenService(InternalClientRepository clientRepository) {
        return new InternalTokenServiceImpl(clientRepository, signingKey, issuer, tokenTtlSeconds);
    }

    @Bean
    public AuditService auditService(AuditEventRepository repository) {
        return new AuditServiceImpl(repository);
    }

    @Bean
    public OutboxService outboxService(OutboxEventRepository repository) {
        return new OutboxServiceImpl(repository);
    }

    @Bean
    public IdempotencyService idempotencyService(IdempotencyRecordRepository repository) {
        return new IdempotencyServiceImpl(repository, recordTtlHours);
    }
}