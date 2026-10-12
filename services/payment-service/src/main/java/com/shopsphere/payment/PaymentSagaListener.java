package com.shopsphere.payment;

import com.shopsphere.common.events.OrderEvents;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentSagaListener {
    private final RabbitTemplate events;
    @Value("${shopsphere.payment.failure-rate:0.0}") double failureRate;
    public PaymentSagaListener(RabbitTemplate events) { this.events = events; }
    @RabbitListener(queues = "payment.events")
    public void charge(Map<String, Object> event) {
        UUID orderId = UUID.fromString(String.valueOf(event.get("orderId")));
        String correlationId = String.valueOf(event.getOrDefault("correlationId", "unknown"));
        if (ThreadLocalRandom.current().nextDouble() < failureRate) {
            events.convertAndSend("shopsphere.events", "payment.failed", new OrderEvents.PaymentFailed(orderId, "Mock payment failed", correlationId, Instant.now()));
        } else {
            events.convertAndSend("shopsphere.events", "payment.completed", new OrderEvents.PaymentCompleted(orderId, correlationId, Instant.now()));
        }
    }
}
