package com.shopsphere.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Event payloads published to the shopsphere.events topic exchange. */
public final class OrderEvents {
    public static final String EXCHANGE = "shopsphere.events";
    public static final String ORDER_CREATED = "order.created";
    public static final String STOCK_RESERVED = "stock.reserved";
    public static final String STOCK_REJECTED = "stock.rejected";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";
    public static final String ORDER_CONFIRMED = "order.confirmed";
    public static final String ORDER_CANCELLED = "order.cancelled";

    private OrderEvents() { }

    public record OrderLine(String productId, int quantity) { }
    public record OrderCreated(UUID orderId, String userId, List<OrderLine> items, BigDecimal total, String correlationId, Instant occurredAt) { }
    public record StockReserved(UUID orderId, String userId, BigDecimal total, String correlationId, Instant occurredAt) { }
    public record StockRejected(UUID orderId, String reason, String correlationId, Instant occurredAt) { }
    public record PaymentCompleted(UUID orderId, String correlationId, Instant occurredAt) { }
    public record PaymentFailed(UUID orderId, String reason, String correlationId, Instant occurredAt) { }
    public record OrderConfirmed(UUID orderId, String userId, BigDecimal total, String correlationId, Instant occurredAt) { }
    public record OrderCancelled(UUID orderId, String userId, String reason, String correlationId, Instant occurredAt) { }
}
