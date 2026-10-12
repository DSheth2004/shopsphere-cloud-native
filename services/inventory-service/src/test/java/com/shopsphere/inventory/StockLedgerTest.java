package com.shopsphere.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.shopsphere.common.events.OrderEvents.OrderLine;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StockLedgerTest {
    private final StockLedger ledger = new StockLedger();

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
}
