package com.shopsphere.order;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Orders are persisted in MongoDB, so a user's order history survives restarts. */
@Component
public class OrderStore {
    private final OrderRepository orders;

    OrderStore(OrderRepository orders) { this.orders = orders; }

    Order save(Order order) { return orders.save(order); }

    Order get(UUID id) {
        return orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    List<Order> forUser(String userId) { return orders.findByUserIdOrderByCreatedAtDesc(userId); }

    /** Read-modify-write; synchronized because saga replies for one order can arrive back to back (single replica). */
    synchronized Order transition(UUID id, Order.Status status, String note) {
        Order current = get(id);
        Order next = current.transition(status, note);
        return next == current ? current : orders.save(next);
    }
}
