package com.shopsphere.order;

import com.shopsphere.common.events.OrderEvents;
import java.time.Instant;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Orchestrates the happy path and the compensation path (stock rejected or payment failed). */
@Component
@RabbitListener(queues = "order.saga-replies")
public class OrderSagaListener {
    private final OrderStore orders;
    private final RabbitTemplate events;

    public OrderSagaListener(OrderStore orders, RabbitTemplate events) {
        this.orders = orders;
        this.events = events;
    }

    @RabbitHandler
    public void stockReserved(OrderEvents.StockReserved event) {
        orders.transition(event.orderId(), Order.Status.STOCK_RESERVED, "Stock reserved by inventory-service");
    }

    @RabbitHandler
    public void stockRejected(OrderEvents.StockRejected event) {
        cancel(event.orderId(), event.reason(), event.correlationId());
    }

    @RabbitHandler
    public void paymentCompleted(OrderEvents.PaymentCompleted event) {
        Order order = orders.transition(event.orderId(), Order.Status.CONFIRMED, "Payment captured by payment-service");
        if (order.status() == Order.Status.CONFIRMED) {
            events.convertAndSend(OrderEvents.EXCHANGE, OrderEvents.ORDER_CONFIRMED,
                    new OrderEvents.OrderConfirmed(order.id(), order.userId(), order.total(), event.correlationId(), Instant.now()));
        }
    }

    @RabbitHandler
    public void paymentFailed(OrderEvents.PaymentFailed event) {
        cancel(event.orderId(), event.reason(), event.correlationId());
    }

    private void cancel(java.util.UUID orderId, String reason, String correlationId) {
        Order order = orders.transition(orderId, Order.Status.CANCELLED, reason);
        if (order.status() == Order.Status.CANCELLED) {
            events.convertAndSend(OrderEvents.EXCHANGE, OrderEvents.ORDER_CANCELLED,
                    new OrderEvents.OrderCancelled(order.id(), order.userId(), reason, correlationId, Instant.now()));
        }
    }
}
