package com.shopsphere.notification;

import com.shopsphere.common.events.OrderEvents;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Notification is deliberately decoupled from the checkout path: it only reacts to final saga events. */
@Component
public class NotificationListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private static final int KEEP = 200;
    private final Deque<Notification> recent = new ArrayDeque<>();

    @RabbitListener(queues = "notification.order-confirmed")
    public void confirmed(OrderEvents.OrderConfirmed event) {
        record(new Notification(UUID.randomUUID(), event.userId(), "SUCCESS", "Order confirmed",
                "Your order " + shortId(event.orderId()) + " for ₹" + event.total().toPlainString() + " is confirmed.", event.orderId(), Instant.now()));
    }

    @RabbitListener(queues = "notification.order-cancelled")
    public void cancelled(OrderEvents.OrderCancelled event) {
        record(new Notification(UUID.randomUUID(), event.userId(), "ERROR", "Order cancelled",
                "Order " + shortId(event.orderId()) + " was cancelled: " + event.reason(), event.orderId(), Instant.now()));
    }

    public synchronized List<Notification> forUser(String userId) {
        return recent.stream().filter(n -> n.userId().equals(userId)).toList();
    }

    private synchronized void record(Notification notification) {
        log.info("Email to {}: {} - {}", notification.userId(), notification.title(), notification.message());
        recent.addFirst(notification);
        while (recent.size() > KEEP) recent.removeLast();
    }

    private static String shortId(UUID id) { return "#" + id.toString().substring(0, 8); }

    public record Notification(UUID id, String userId, String level, String title, String message, UUID orderId, Instant at) { }
}
