package com.shopsphere.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopsphere.common.events.OrderEvents.OrderLine;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StockLedgerTest {
    private final StockLedger ledger = new StockLedger(InventoryStore.inMemory());

    @Test
    void reservationIsIdempotentAndReleasable() {
        ledger.set("SKU-X", 5);
        UUID order = UUID.randomUUID();
        assertThat(ledger.reserve(order, List.of(new OrderLine("SKU-X", 3)))).isEmpty();
        assertThat(ledger.reserve(order, List.of(new OrderLine("SKU-X", 3)))).isEmpty();
        assertThat(ledger.availableFor("SKU-X")).isEqualTo(2);
        assertThat(ledger.release(order)).isTrue();
        assertThat(ledger.release(order)).isFalse();
        assertThat(ledger.availableFor("SKU-X")).isEqualTo(5);
    }

    @Test
    void insufficientStockRejectsWholeOrder() {
        ledger.set("SKU-A", 10);
        ledger.set("SKU-B", 1);
        assertThat(ledger.reserve(UUID.randomUUID(), List.of(new OrderLine("SKU-A", 2), new OrderLine("SKU-B", 2))))
                .hasValueSatisfying(reason -> assertThat(reason).contains("SKU-B"));
        assertThat(ledger.availableFor("SKU-A")).isEqualTo(10);
    }

    @Test
    void stockAndReservationsSurviveARestart() {
        InventoryStore store = new RecordingStore();
        StockLedger before = new StockLedger(store);
        UUID order = UUID.randomUUID();
        assertThat(before.reserve(order, List.of(new OrderLine("SKU-1004", 2)))).isEmpty();

        StockLedger after = new StockLedger(store);
        assertThat(after.availableFor("SKU-1004")).isEqualTo(1);
        assertThat(after.release(order)).isTrue();
        assertThat(new StockLedger(store).availableFor("SKU-1004")).isEqualTo(3);
    }

    /** Keeps what the ledger writes, like MongoDB would across a restart. */
    private static final class RecordingStore implements InventoryStore {
        private final Map<String, Integer> stock = new HashMap<>();
        private final Map<UUID, List<OrderLine>> reservations = new HashMap<>();
        public Map<String, Integer> loadStock() { return Map.copyOf(stock); }
        public Map<UUID, List<OrderLine>> loadReservations() { return Map.copyOf(reservations); }
        public void saveStock(Map<String, Integer> levels) { stock.putAll(levels); }
        public void saveReservation(UUID orderId, List<OrderLine> lines) { reservations.put(orderId, lines); }
        public void deleteReservation(UUID orderId) { reservations.remove(orderId); }
    }
}
