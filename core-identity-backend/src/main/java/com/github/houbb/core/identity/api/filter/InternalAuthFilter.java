package com.github.houbb.core.identity.api.filter;

import com.github.houbb.core.identity.application.service.InternalTokenService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Internal API authentication filter.
 * Validates service tokens for /internal/ endpoints.
 */
public class InternalAuthFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(InternalAuthFilter.class);

    private final InternalTokenService tokenService;

    public InternalAuthFilter(InternalTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request,
                         jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String path = httpReq.getRequestURI();
        if (!path.startsWith("/internal/")) {
            chain.doFilter(request, response);
            return;
        }

        // Skip auth for token endpoint itself
        if ("POST".equals(httpReq.getMethod()) && path.equals("/internal/v1/identity/service-tokens")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpReq.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResp.setStatus(401);
            httpResp.setContentType("application/json");
            httpResp.getWriter().write("{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Missing or invalid Authorization header\"}");
            return;
        }

        String token = authHeader.substring(7);
        try {
            tokenService.validateToken(token);
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("Internal API auth failed: {}", e.getMessage());
            httpResp.setStatus(401);
            httpResp.setContentType("application/json");
            httpResp.getWriter().write("{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Invalid service token\"}");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}