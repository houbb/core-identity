package com.github.houbb.core.identity.infrastructure.database;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * H2 database adapter (dev/test only).
 */
@Component
@ConditionalOnProperty(name = "core.database.type", havingValue = "h2")
public class H2DatabaseAdapter implements DatabaseAdapter {

    private final DataSource dataSource;

    public H2DatabaseAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.H2;
    }
}
