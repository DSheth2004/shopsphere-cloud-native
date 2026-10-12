package com.shopsphere.inventory;

import com.shopsphere.common.events.OrderEvents;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {
    public static void main(String[] args) { SpringApplication.run(InventoryServiceApplication.class, args); }

    @Bean Queue inventoryOrders() { return new Queue("inventory.order-created", true); }
    @Bean Queue inventoryCompensations() { return new Queue("inventory.order-cancelled", true); }
    @Bean Binding inventoryOrdersBinding(Queue inventoryOrders, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(inventoryOrders).to(shopsphereEvents).with(OrderEvents.ORDER_CREATED);
    }
    @Bean Binding inventoryCompensationsBinding(Queue inventoryCompensations, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(inventoryCompensations).to(shopsphereEvents).with(OrderEvents.ORDER_CANCELLED);
    }
}
