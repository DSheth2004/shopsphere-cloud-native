package com.shopsphere.inventory;

import com.shopsphere.common.events.OrderEvents.OrderLine;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Stock levels plus per-order reservations. Reservations are keyed by order id, so a redelivered
 * message never reserves twice, and a cancelled order releases exactly what it held.
 * Every change is written through to the {@link InventoryStore}, so stock survives restarts.
 */
@Component
public class StockLedger {
    private final Map<String, Integer> available = new TreeMap<>();
    private final Map<UUID, List<OrderLine>> reservations = new HashMap<>();

    private final InventoryStore store;

    public StockLedger(InventoryStore store) {
        this.store = store;
        available.putAll(store.loadStock());
        reservations.putAll(store.loadReservations());
        if (available.isEmpty()) {
            for (int i = 1; i <= 12; i++) available.put(String.format("SKU-%04d", 1000 + i), i % 4 == 0 ? 3 : 25);
            store.saveStock(available);
        }
    }

    public synchronized Map<String, Integer> snapshot() { return Map.copyOf(available); }

    public synchronized int availableFor(String sku) { return available.getOrDefault(sku, 0); }

    public synchronized void set(String sku, int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        available.put(sku, quantity);
        store.saveStock(Map.of(sku, quantity));
    }

    /** @return empty when reserved (or already reserved), otherwise the reason for rejecting the order. */
    public synchronized Optional<String> reserve(UUID orderId, List<OrderLine> lines) {
        if (reservations.containsKey(orderId)) return Optional.empty();
        Map<String, Integer> wanted = new HashMap<>();
        lines.forEach(line -> wanted.merge(line.productId(), line.quantity(), Integer::sum));
        for (Map.Entry<String, Integer> entry : wanted.entrySet()) {
            int stock = available.getOrDefault(entry.getKey(), 0);
            if (stock < entry.getValue()) {
                return Optional.of("Insufficient stock for " + entry.getKey() + " (available " + stock + ")");
            }
        }
        wanted.forEach((sku, quantity) -> available.merge(sku, -quantity, Integer::sum));
        reservations.put(orderId, List.copyOf(lines));
        store.saveStock(levelsOf(wanted.keySet()));
        store.saveReservation(orderId, List.copyOf(lines));
        return Optional.empty();
    }

    /** Compensating action for the saga: returns reserved stock when an order is cancelled. */
    public synchronized boolean release(UUID orderId) {
        List<OrderLine> lines = reservations.remove(orderId);
        if (lines == null) return false;
        lines.forEach(line -> available.merge(line.productId(), line.quantity(), Integer::sum));
        store.saveStock(levelsOf(lines.stream().map(OrderLine::productId).collect(Collectors.toSet())));
        store.deleteReservation(orderId);
        return true;
    }

    private Map<String, Integer> levelsOf(Set<String> skus) {
        Map<String, Integer> levels = new HashMap<>();
        skus.forEach(sku -> levels.put(sku, available.get(sku)));
        return levels;
    }
}
