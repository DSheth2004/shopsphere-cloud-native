package com.shopsphere.inventory;

import com.shopsphere.common.events.OrderEvents.OrderLine;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

/** Persists stock levels and per-order reservations in the inventory_db MongoDB database. */
@Component
class MongoInventoryStore implements InventoryStore {
    private final StockRepository stock;
    private final ReservationRepository reservations;

    MongoInventoryStore(StockRepository stock, ReservationRepository reservations) {
        this.stock = stock;
        this.reservations = reservations;
    }

    @Override public Map<String, Integer> loadStock() {
        return stock.findAll().stream().collect(Collectors.toMap(StockLevel::sku, StockLevel::available));
    }

    @Override public Map<UUID, List<OrderLine>> loadReservations() {
        return reservations.findAll().stream().collect(Collectors.toMap(Reservation::orderId, Reservation::lines));
    }

    @Override public void saveStock(Map<String, Integer> levels) {
        stock.saveAll(levels.entrySet().stream().map(e -> new StockLevel(e.getKey(), e.getValue())).toList());
    }

    @Override public void saveReservation(UUID orderId, List<OrderLine> lines) { reservations.save(new Reservation(orderId, lines)); }

    @Override public void deleteReservation(UUID orderId) { reservations.deleteById(orderId); }

    @Document("stock")
    record StockLevel(@Id String sku, int available) { }

    @Document("reservations")
    record Reservation(@Id UUID orderId, List<OrderLine> lines) { }
}
