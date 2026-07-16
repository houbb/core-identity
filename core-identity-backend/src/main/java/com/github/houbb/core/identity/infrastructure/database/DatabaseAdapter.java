package com.github.houbb.core.identity.infrastructure.database;

import javax.sql.DataSource;

/**
 * Database adapter abstraction.
 * <p>
 * Provides runtime database type information and read-replica awareness.
 * Different implementations handle dialect-specific behaviors
 * (e.g. SQLite has no SELECT FOR UPDATE, MySQL has different locking semantics).
 */
public interface DatabaseAdapter {

    /**
     * Returns the primary DataSource.
     */
    DataSource getDataSource();

    /**
     * Returns the database type.
     */
    DatabaseType getType();

    /**
     * Whether a read replica is available for query offloading.
     * Default: false (single database).
     */
    default boolean isReadReplicaAvailable() {
        return false;
    }

    /**
     * Returns the read replica DataSource, or the primary if no replica exists.
     */
    default DataSource getReadDataSource() {
        return getDataSource();
    }

    /**
     * Whether the database supports SELECT ... FOR UPDATE (row-level locking).
     */
    default boolean supportsSelectForUpdate() {
        return getType() != DatabaseType.SQLITE;
    }

    /**
     * Whether the database supports INSERT ... ON CONFLICT (upsert).
     */
    default boolean supportsUpsert() {
        return getType() == DatabaseType.POSTGRESQL || getType() == DatabaseType.SQLITE;
    }

    /**
     * Whether the database supports INSERT ... ON DUPLICATE KEY UPDATE (MySQL-style upsert).
     */
    default boolean supportsOnDuplicateKeyUpdate() {
        return getType() == DatabaseType.MYSQL || getType() == DatabaseType.H2;
    }
}
