package com.shopsphere.notification;
import com.shopsphere.common.web.CorrelationIdFilter;
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
    @Bean CorrelationIdFilter correlationIdFilter() { return new CorrelationIdFilter(); }
    @Bean TopicExchange shopsphereEvents() { return new TopicExchange("shopsphere.events", true, false); }
    @Bean Queue notificationEvents() { return new Queue("notification.events", true); }
    @Bean Binding confirmedOrders(Queue notificationEvents, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(notificationEvents).to(shopsphereEvents).with("order.confirmed");
    }
    @Bean Binding cancelledOrders(Queue notificationEvents, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(notificationEvents).to(shopsphereEvents).with("order.cancelled");
    }
}
