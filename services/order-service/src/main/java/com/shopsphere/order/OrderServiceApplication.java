package com.shopsphere.order;

import com.shopsphere.common.events.OrderEvents;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) { SpringApplication.run(OrderServiceApplication.class, args); }

    @Bean RestClient productClient(@Value("${shopsphere.product-service-url}") String baseUrl) {
        JdkClientHttpRequestFactory requests = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build());
        requests.setReadTimeout(Duration.ofSeconds(3));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(requests).build();
    }

    @Bean Queue orderSagaReplies() { return new Queue("order.saga-replies", true); }
    @Bean Binding stockReservedBinding(Queue orderSagaReplies, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(orderSagaReplies).to(shopsphereEvents).with(OrderEvents.STOCK_RESERVED);
    }
    @Bean Binding stockRejectedBinding(Queue orderSagaReplies, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(orderSagaReplies).to(shopsphereEvents).with(OrderEvents.STOCK_REJECTED);
    }
    @Bean Binding paymentCompletedBinding(Queue orderSagaReplies, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(orderSagaReplies).to(shopsphereEvents).with(OrderEvents.PAYMENT_COMPLETED);
    }
    @Bean Binding paymentFailedBinding(Queue orderSagaReplies, TopicExchange shopsphereEvents) {
        return BindingBuilder.bind(orderSagaReplies).to(shopsphereEvents).with(OrderEvents.PAYMENT_FAILED);
    }
}
