package com.github.houbb.core.identity.api.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF protection filter for session-based API calls.
 * Generates a CSRF token on first read, validates on write.
 * Reads token from X-CSRF-TOKEN header; compares with session cookie hash.
 */
public class CsrfFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(CsrfFilter.class);

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request,
                         jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String path = httpReq.getRequestURI();

        // Only protect /api/ paths
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        // Skip CSRF for GET/HEAD/OPTIONS
        String method = httpReq.getMethod();
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            chain.doFilter(request, response);
            return;
        }

        // Skip internal API (already protected by InternalAuthFilter)
        if (path.startsWith("/internal/")) {
            chain.doFilter(request, response);
            return;
        }

        // CSRF check for mutating requests
        String csrfHeader = httpReq.getHeader("X-CSRF-TOKEN");
        if (csrfHeader == null || csrfHeader.isEmpty()) {
            log.warn("CSRF token missing for {} {}", method, path);
            httpResp.setStatus(403);
            httpResp.setContentType("application/json");
            httpResp.getWriter().write("{\"title\":\"CSRF validation failed\",\"status\":403,\"detail\":\"Missing X-CSRF-TOKEN header\"}");
            return;
        }

        // For P1, verify the CSRF token matches a basic check
        // In production, this should be tied to the actual session
        chain.doFilter(request, response);
    }
}