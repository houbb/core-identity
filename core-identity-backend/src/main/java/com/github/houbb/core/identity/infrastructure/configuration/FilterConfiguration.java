package com.github.houbb.core.identity.infrastructure.configuration;

import com.github.houbb.core.identity.api.filter.InternalAuthFilter;
import com.github.houbb.core.identity.api.filter.RequestIdFilter;
import com.github.houbb.core.identity.application.service.InternalTokenService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filter registration.
 */
@Configuration
public class FilterConfiguration {

    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilter() {
        FilterRegistrationBean<RequestIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<InternalAuthFilter> internalAuthFilter(InternalTokenService tokenService) {
        FilterRegistrationBean<InternalAuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new InternalAuthFilter(tokenService));
        registration.addUrlPatterns("/internal/*");
        registration.setOrder(2);
        return registration;
    }
}