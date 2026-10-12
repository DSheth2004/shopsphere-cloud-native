package com.shopsphere.inventory;

import com.shopsphere.common.events.OrderEvents;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Saga participant: reserves stock for new orders and releases it when an order is cancelled. */
@Component
public class InventorySagaListener {
    private static final Logger log = LoggerFactory.getLogger(InventorySagaListener.class);
    private final StockLedger ledger;
    private final RabbitTemplate events;

    public InventorySagaListener(StockLedger ledger, RabbitTemplate events) {
        this.ledger = ledger;
        this.events = events;
    }

    @RabbitListener(queues = "inventory.order-created")
    public void reserve(OrderEvents.OrderCreated order) {
        ledger.reserve(order.orderId(), order.items()).ifPresentOrElse(
                reason -> {
                    log.info("Rejected order {}: {}", order.orderId(), reason);
                    events.convertAndSend(OrderEvents.EXCHANGE, OrderEvents.STOCK_REJECTED,
                            new OrderEvents.StockRejected(order.orderId(), reason, order.correlationId(), Instant.now()));
                },
                () -> events.convertAndSend(OrderEvents.EXCHANGE, OrderEvents.STOCK_RESERVED,
                        new OrderEvents.StockReserved(order.orderId(), order.userId(), order.total(), order.correlationId(), Instant.now())));
    }

    @RabbitListener(queues = "inventory.order-cancelled")
    public void release(OrderEvents.OrderCancelled cancelled) {
        if (ledger.release(cancelled.orderId())) log.info("Released stock held by cancelled order {}", cancelled.orderId());
    }
}
