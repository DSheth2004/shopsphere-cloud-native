package com.shopsphere.common.api;

import java.time.Instant;
import java.util.Map;

/** Standard error body returned by ShopSphere HTTP services. */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String correlationId,
        Map<String, String> fieldErrors) { }
