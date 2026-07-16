package com.github.houbb.core.identity.infrastructure.observability;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Trace context utility (P7.5).
 * <p>
 * Manages trace_id and request_id in the MDC context for
 * end-to-end request tracing across services.
 */
public final class TraceContext {

    private TraceContext() {
    }

    public static final String TRACE_ID_KEY = "traceId";
    public static final String REQUEST_ID_KEY = "requestId";
    public static final String ORGANIZATION_ID_KEY = "organizationId";
    public static final String CLIENT_ID_KEY = "clientId";

    /**
     * Set the trace ID for the current request.
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * Get the current trace ID.
     */
    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID_KEY, traceId);
        }
        return traceId;
    }

    /**
     * Set the request ID for the current request.
     */
    public static void setRequestId(String requestId) {
        MDC.put(REQUEST_ID_KEY, requestId);
    }

    /**
     * Get the current request ID.
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }

    /**
     * Set the organization context for the current request.
     */
    public static void setOrganizationId(String organizationId) {
        if (organizationId != null) {
            MDC.put(ORGANIZATION_ID_KEY, organizationId);
        }
    }

    /**
     * Set the client context for the current request.
     */
    public static void setClientId(String clientId) {
        if (clientId != null) {
            MDC.put(CLIENT_ID_KEY, clientId);
        }
    }

    /**
     * Clear all trace context (call at end of request processing).
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
        MDC.remove(REQUEST_ID_KEY);
        MDC.remove(ORGANIZATION_ID_KEY);
        MDC.remove(CLIENT_ID_KEY);
    }
}
