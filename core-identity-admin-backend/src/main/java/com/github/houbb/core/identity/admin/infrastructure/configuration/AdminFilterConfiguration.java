package com.github.houbb.core.identity.admin.infrastructure.configuration;

import com.github.houbb.core.identity.admin.api.filter.AdminAccessFilter;
import com.github.houbb.core.identity.admin.api.filter.AdminRequestIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Filter configuration for admin backend.
 */
@Configuration
public class AdminFilterConfiguration {

    @Bean
    public FilterRegistrationBean<AdminRequestIdFilter> adminRequestIdFilter() {
        FilterRegistrationBean<AdminRequestIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AdminRequestIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AdminAccessFilter> adminAccessFilter() {
        FilterRegistrationBean<AdminAccessFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AdminAccessFilter());
        registration.addUrlPatterns("/admin-api/*");
        registration.setOrder(2);
        return registration;
    }
}