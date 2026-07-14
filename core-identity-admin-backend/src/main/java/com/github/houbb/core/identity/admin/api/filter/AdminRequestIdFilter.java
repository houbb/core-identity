package com.github.houbb.core.identity.admin.api.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

/**
 * Request ID pass-through filter for admin backend.
 */
public class AdminRequestIdFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AdminRequestIdFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String requestId = httpReq.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        httpResp.setHeader("X-Request-ID", requestId);

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Request failed [requestId={}]: {}", requestId, e.getMessage());
            throw e;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}