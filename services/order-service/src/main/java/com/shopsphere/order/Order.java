package com.shopsphere.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Immutable order snapshot stored in MongoDB; every saga transition appends to the history shown in the UI. */
@Document("orders")
public record Order(@Id UUID id, String userId, List<Line> items, BigDecimal total, Status status, String reason,
                    Instant createdAt, List<Step> history) {

    public enum Status { PENDING, STOCK_RESERVED, CONFIRMED, CANCELLED;
        boolean isFinal() { return this == CONFIRMED || this == CANCELLED; }
    }

    public record Line(String productId, String name, String image, BigDecimal unitPrice, int quantity) {
        BigDecimal lineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    }

    public record Step(Status status, String note, Instant at) { }

    static Order create(String userId, List<Line> items) {
        Instant now = Instant.now();
        BigDecimal total = items.stream().map(Line::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Order(UUID.randomUUID(), userId, List.copyOf(items), total, Status.PENDING, null, now,
                List.of(new Step(Status.PENDING, "Order placed", now)));
    }

    /** Moves the order forward; transitions out of a final state are ignored so redelivered events are harmless. */
    Order transition(Status next, String note) {
        if (status.isFinal() || status == next) return this;
        List<Step> steps = new ArrayList<>(history);
        steps.add(new Step(next, note, Instant.now()));
        return new Order(id, userId, items, total, next, next == Status.CANCELLED ? note : reason, createdAt, List.copyOf(steps));
    }
}
