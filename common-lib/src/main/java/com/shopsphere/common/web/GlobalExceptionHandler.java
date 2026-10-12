package com.shopsphere.common.web;

import com.shopsphere.common.api.ApiError;
import java.time.Instant;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Keeps API errors predictable without exposing stack traces to clients. */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException exception) {
        ApiError body = new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST",
                exception.getMessage(), MDC.get("correlationId"), Map.of());
        return ResponseEntity.badRequest().body(body);
    }
}
