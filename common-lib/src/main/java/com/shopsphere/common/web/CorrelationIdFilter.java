package com.shopsphere.common.web;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

/** Propagates x-request-id so logs and events for one request can be traced. */
public final class CorrelationIdFilter implements Filter {
    public static final String HEADER = "x-request-id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String correlationId = httpRequest.getHeader(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        try (MDC.MDCCloseable ignored = MDC.putCloseable("correlationId", correlationId)) {
            httpResponse.setHeader(HEADER, correlationId);
            chain.doFilter(request, response);
        }
    }
}
