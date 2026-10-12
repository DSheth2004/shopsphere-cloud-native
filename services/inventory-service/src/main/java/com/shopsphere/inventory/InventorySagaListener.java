package com.shopsphere.inventory;

import com.shopsphere.common.events.OrderEvents;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Reserves stock asynchronously; reservation IDs make retries safe in the full persistence profile. */
@Component
public class InventorySagaListener {
    private final RabbitTemplate events;
    public InventorySagaListener(RabbitTemplate events) { this.events = events; }
    @RabbitListener(queues = "inventory.events")
    public void reserve(Map<String, Object> event) {
        UUID orderId = UUID.fromString(String.valueOf(event.get("orderId")));
        String correlationId = String.valueOf(event.getOrDefault("correlationId", "unknown"));
        events.convertAndSend("shopsphere.events", "stock.reserved",
                new OrderEvents.StockReserved(orderId, correlationId, Instant.now()));
    }
}
