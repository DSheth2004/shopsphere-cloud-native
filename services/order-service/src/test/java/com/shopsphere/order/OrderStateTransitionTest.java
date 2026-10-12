package com.shopsphere.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderStateTransitionTest {
    private final Order order = Order.create("ada", List.of(
            new Order.Line("SKU-1", "Mug", "🍵", new BigDecimal("250.00"), 2),
            new Order.Line("SKU-2", "Lamp", "💡", new BigDecimal("1000.00"), 1)));

    @Test
    void totalIsComputedFromLines() {
        assertThat(order.total()).isEqualByComparingTo("1500.00");
        assertThat(order.status()).isEqualTo(Order.Status.PENDING);
    }

    @Test
    void happyPathRecordsEveryStep() {
        Order confirmed = order.transition(Order.Status.STOCK_RESERVED, "reserved").transition(Order.Status.CONFIRMED, "paid");
        assertThat(confirmed.history()).extracting(Order.Step::status)
                .containsExactly(Order.Status.PENDING, Order.Status.STOCK_RESERVED, Order.Status.CONFIRMED);
    }

    @Test
    void finalStatesIgnoreLateEvents() {
        Order cancelled = order.transition(Order.Status.CANCELLED, "Payment declined");
        assertThat(cancelled.reason()).isEqualTo("Payment declined");
        assertThat(cancelled.transition(Order.Status.CONFIRMED, "late").status()).isEqualTo(Order.Status.CANCELLED);
    }
}
