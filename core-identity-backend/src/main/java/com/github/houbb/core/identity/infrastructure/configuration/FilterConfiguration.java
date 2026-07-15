package com.github.houbb.core.identity.infrastructure.configuration;

import com.github.houbb.core.identity.api.filter.CsrfFilter;
import com.github.houbb.core.identity.api.filter.InternalAuthFilter;
import com.github.houbb.core.identity.api.filter.RateLimitFilter;
import com.github.houbb.core.identity.api.filter.RequestIdFilter;
import com.github.houbb.core.identity.application.service.InternalTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filter registration.
 */
@Configuration
public class FilterConfiguration {

    @Value("${core.login.ip-rate-limit:20}")
    private int ipRateLimit;

    @Value("${core.login.ip-rate-window-ms:60000}")
    private long ipRateWindowMs;

    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
        FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter(ipRateLimit, ipRateWindowMs));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CsrfFilter> csrfFilter() {
        FilterRegistrationBean<CsrfFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CsrfFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(3);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<InternalAuthFilter> internalAuthFilter(InternalTokenService tokenService) {
        FilterRegistrationBean<InternalAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new InternalAuthFilter(tokenService));
        registration.addUrlPatterns("/internal/*");
        registration.setOrder(4);
        return registration;
    }
}