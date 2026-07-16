package com.github.houbb.core.identity.infrastructure.database;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * SQLite database adapter.
 */
@Component
@ConditionalOnProperty(name = "core.database.type", havingValue = "sqlite", matchIfMissing = true)
public class SqliteDatabaseAdapter implements DatabaseAdapter {

    private final DataSource dataSource;

    public SqliteDatabaseAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.SQLITE;
    }
}
