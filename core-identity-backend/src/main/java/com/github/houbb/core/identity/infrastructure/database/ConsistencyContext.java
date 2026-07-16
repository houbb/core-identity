package com.github.houbb.core.identity.infrastructure.database;

/**
 * Thread-local context for read-after-write consistency.
 * <p>
 * After performing a write operation (password change, session revocation, role modification),
 * set PRIMARY_REQUIRED so subsequent reads in the same request hit the primary database
 * instead of a potentially stale read replica.
 */
public final class ConsistencyContext {

    private static final ThreadLocal<ConsistencyLevel> LEVEL = ThreadLocal.withInitial(() -> ConsistencyLevel.REPLICA_ALLOWED);

    private ConsistencyContext() {
    }

    public enum ConsistencyLevel {
        /** Read from primary to guarantee read-after-write consistency. */
        PRIMARY_REQUIRED,
        /** Read from replica is acceptable. */
        REPLICA_ALLOWED
    }

    /**
     * Set the consistency level for the current request.
     */
    public static void set(ConsistencyLevel level) {
        LEVEL.set(level);
    }

    /**
     * Get the current consistency level.
     */
    public static ConsistencyLevel get() {
        return LEVEL.get();
    }

    /**
     * Require primary for the remaining duration of this request.
     */
    public static void requirePrimary() {
        LEVEL.set(ConsistencyLevel.PRIMARY_REQUIRED);
    }

    /**
     * Clear the thread-local (called by filter at end of request).
     */
    public static void clear() {
        LEVEL.remove();
    }

    /**
     * Whether primary reads are required.
     */
    public static boolean isPrimaryRequired() {
        return LEVEL.get() == ConsistencyLevel.PRIMARY_REQUIRED;
    }
}
