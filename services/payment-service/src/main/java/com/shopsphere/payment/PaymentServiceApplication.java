package com.shopsphere.payment;

import com.shopsphere.common.events.OrderEvents;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PaymentServiceApplication {
    public static void main(String[] args) { SpringApplication.run(PaymentServiceApplication.class, args); }

    @Bean Queue paymentRequests() { return new Queue("payment.stock-reserved", true); }
    @Bean Binding paymentBinding(Queue paymentRequests, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(paymentRequests).to(shopsphereEvents).with(OrderEvents.STOCK_RESERVED);
    }
}
