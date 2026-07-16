package com.github.houbb.core.identity.infrastructure.database;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * MySQL database adapter.
 */
@Component
@ConditionalOnProperty(name = "core.database.type", havingValue = "mysql")
public class MySqlDatabaseAdapter implements DatabaseAdapter {

    private final DataSource dataSource;

    public MySqlDatabaseAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MYSQL;
    }
}
