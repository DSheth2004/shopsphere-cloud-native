package com.shopsphere.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Event payloads published to the shopsphere.events topic exchange. */
public final class OrderEvents {
    private OrderEvents() { }

    public record OrderCreated(UUID orderId, String userId, BigDecimal total, String correlationId, Instant occurredAt) { }
    public record StockReserved(UUID orderId, String correlationId, Instant occurredAt) { }
    public record StockRejected(UUID orderId, String reason, String correlationId, Instant occurredAt) { }
    public record PaymentCompleted(UUID orderId, String correlationId, Instant occurredAt) { }
    public record PaymentFailed(UUID orderId, String reason, String correlationId, Instant occurredAt) { }
    public record OrderConfirmed(UUID orderId, String userId, String correlationId, Instant occurredAt) { }
    public record OrderCancelled(UUID orderId, String userId, String reason, String correlationId, Instant occurredAt) { }
}
