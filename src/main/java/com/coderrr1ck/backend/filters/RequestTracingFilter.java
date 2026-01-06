package com.coderrr1ck.backend.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class RequestTracingFilter extends OncePerRequestFilter {
    public static final String MDC_KEY = "requestId";
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();

        try {
            MDC.put(MDC_KEY, requestId);
            request.setAttribute("RequestId", requestId);

            log.info("Incoming request [{}] {} {}",
                    requestId,
                    request.getMethod(),
                    request.getRequestURI());

            filterChain.doFilter(request, response);

        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
