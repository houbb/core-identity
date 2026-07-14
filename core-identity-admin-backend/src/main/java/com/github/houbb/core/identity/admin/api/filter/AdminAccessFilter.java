package com.github.houbb.core.identity.admin.api.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

/**
 * Development access protection filter.
 * In production (development-access=false), restricts access to localhost only.
 */
public class AdminAccessFilter implements Filter {

    @Value("${core.admin.development-access:false}")
    private boolean developmentAccess;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        if (developmentAccess) {
            chain.doFilter(request, response);
            return;
        }

        // Production — restrict to localhost
        String remoteAddr = httpReq.getRemoteAddr();
        if ("127.0.0.1".equals(remoteAddr) || "0:0:0:0:0:0:0:1".equals(remoteAddr) || "localhost".equals(remoteAddr)) {
            chain.doFilter(request, response);
            return;
        }

        httpResp.setStatus(403);
        httpResp.setContentType("application/json");
        httpResp.getWriter().write(
                "{\"title\":\"Forbidden\",\"status\":403," +
                "\"detail\":\"Admin console access is restricted in production mode.\"}"
        );
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}