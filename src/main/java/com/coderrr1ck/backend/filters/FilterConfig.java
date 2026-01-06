package com.coderrr1ck.backend.filters;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestTracingFilter> requestIdFilter() {
        FilterRegistrationBean<RequestTracingFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(new RequestTracingFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}

