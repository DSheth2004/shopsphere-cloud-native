package com.shopsphere.order;

import com.shopsphere.common.events.OrderEvents;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Orchestrates the happy path and payment-failure compensation states. */
@Component
public class OrderSagaListener {
    private final OrderController orders;
    private final RabbitTemplate events;
    public OrderSagaListener(OrderController orders, RabbitTemplate events) { this.orders = orders; this.events = events; }
    @RabbitListener(queues = "order.stock.events")
    public void stockReserved(Map<String, Object> event) {
        UUID id = UUID.fromString(String.valueOf(event.get("orderId")));
        orders.transition(id, OrderController.Status.STOCK_RESERVED);
    }
    @RabbitListener(queues = "order.payment.events")
    public void paymentResult(Map<String, Object> event) {
        UUID id = UUID.fromString(String.valueOf(event.get("orderId")));
        String correlationId = String.valueOf(event.getOrDefault("correlationId", "unknown"));
        if (event.containsKey("reason")) {
            OrderController.Order order = orders.transition(id, OrderController.Status.CANCELLED);
            events.convertAndSend("shopsphere.events", "order.cancelled", new OrderEvents.OrderCancelled(id, order.userId(), String.valueOf(event.get("reason")), correlationId, Instant.now()));
        } else {
            OrderController.Order order = orders.transition(id, OrderController.Status.CONFIRMED);
            events.convertAndSend("shopsphere.events", "order.confirmed", new OrderEvents.OrderConfirmed(id, order.userId(), correlationId, Instant.now()));
        }
    }
}
