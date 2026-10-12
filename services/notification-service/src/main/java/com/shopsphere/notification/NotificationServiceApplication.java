package com.shopsphere.notification;

import com.shopsphere.common.events.OrderEvents;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) { SpringApplication.run(NotificationServiceApplication.class, args); }

    @Bean Queue confirmedOrders() { return new Queue("notification.order-confirmed", true); }
    @Bean Queue cancelledOrders() { return new Queue("notification.order-cancelled", true); }
    @Bean Binding confirmedBinding(Queue confirmedOrders, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(confirmedOrders).to(shopsphereEvents).with(OrderEvents.ORDER_CONFIRMED);
    }
    @Bean Binding cancelledBinding(Queue cancelledOrders, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(cancelledOrders).to(shopsphereEvents).with(OrderEvents.ORDER_CANCELLED);
    }
}
