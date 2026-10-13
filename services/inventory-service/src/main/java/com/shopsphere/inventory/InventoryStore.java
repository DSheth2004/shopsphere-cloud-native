package com.shopsphere.inventory;

import com.shopsphere.common.events.OrderEvents.OrderLine;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Where the ledger keeps stock and reservations; MongoDB in the service, in memory in unit tests. */
public interface InventoryStore {
    Map<String, Integer> loadStock();
    Map<UUID, List<OrderLine>> loadReservations();
    void saveStock(Map<String, Integer> levels);
    void saveReservation(UUID orderId, List<OrderLine> lines);
    void deleteReservation(UUID orderId);

    /** A store that remembers nothing, for unit tests of the ledger logic. */
    static InventoryStore inMemory() {
        return new InventoryStore() {
            public Map<String, Integer> loadStock() { return Map.of(); }
            public Map<UUID, List<OrderLine>> loadReservations() { return Map.of(); }
            public void saveStock(Map<String, Integer> levels) { }
            public void saveReservation(UUID orderId, List<OrderLine> lines) { }
            public void deleteReservation(UUID orderId) { }
        };
    }
}
