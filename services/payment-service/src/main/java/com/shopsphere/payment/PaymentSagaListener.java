package com.shopsphere.payment;

import com.shopsphere.common.events.OrderEvents;
import java.time.Instant;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Saga participant: charges the customer once stock is reserved. */
@Component
public class PaymentSagaListener {
    private final PaymentProcessor processor;
    private final RabbitTemplate events;

    public PaymentSagaListener(PaymentProcessor processor, RabbitTemplate events) {
        this.processor = processor;
        this.events = events;
    }

    @RabbitListener(queues = "payment.stock-reserved")
    public void charge(OrderEvents.StockReserved event) {
        PaymentProcessor.Result result = processor.charge(event.total());
        if (result.approved()) {
            events.convertAndSend(OrderEvents.EXCHANGE, OrderEvents.PAYMENT_COMPLETED,
                    new OrderEvents.PaymentCompleted(event.orderId(), event.correlationId(), Instant.now()));
        } else {
            events.convertAndSend(OrderEvents.EXCHANGE, OrderEvents.PAYMENT_FAILED,
                    new OrderEvents.PaymentFailed(event.orderId(), result.reason(), event.correlationId(), Instant.now()));
        }
    }
}
