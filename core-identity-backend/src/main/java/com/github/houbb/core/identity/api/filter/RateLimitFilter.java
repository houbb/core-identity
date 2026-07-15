package com.github.houbb.core.identity.api.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-process rate limiting filter.
 * For multi-instance deployment, replace with Redis-based limiter.
 */
public class RateLimitFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final int maxRequests;
    private final long windowMs;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request,
                         jakarta.servlet.ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String path = httpReq.getRequestURI();

        // Only rate limit auth and login endpoints
        if (!path.contains("/auth/login") && !path.contains("/auth/register") && !path.contains("/password-resets")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(httpReq);
        String key = ip + ":" + path;
        long now = System.currentTimeMillis();

        WindowCounter counter = counters.computeIfAbsent(key, k -> new WindowCounter(now + windowMs));
        synchronized (counter) {
            if (now > counter.windowEnd) {
                counter.count = 0;
                counter.windowEnd = now + windowMs;
            }
            counter.count++;

            if (counter.count > maxRequests) {
                log.warn("Rate limit exceeded: {} for {}", ip, path);
                httpResp.setStatus(429);
                httpResp.setContentType("application/json");
                httpResp.getWriter().write("{\"title\":\"Rate limit exceeded\",\"status\":429,\"detail\":\"Too many requests\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    static class WindowCounter {
        int count;
        long windowEnd;

        WindowCounter(long windowEnd) {
            this.count = 0;
            this.windowEnd = windowEnd;
        }
    }
}