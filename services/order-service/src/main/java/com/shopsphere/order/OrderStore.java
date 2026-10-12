package com.shopsphere.order;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class OrderStore {
    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();

    Order save(Order order) { orders.put(order.id(), order); return order; }

    Order get(UUID id) {
        Order order = orders.get(id);
        if (order == null) throw new NoSuchElementException("Order not found");
        return order;
    }

    List<Order> forUser(String userId) {
        return orders.values().stream().filter(order -> order.userId().equals(userId))
                .sorted(Comparator.comparing(Order::createdAt).reversed()).toList();
    }

    Order transition(UUID id, Order.Status status, String note) {
        return orders.compute(id, (key, current) -> {
            if (current == null) throw new NoSuchElementException("Order not found");
            return current.transition(status, note);
        });
    }
}
