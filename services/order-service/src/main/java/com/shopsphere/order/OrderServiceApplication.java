package com.shopsphere.order;
import com.shopsphere.common.web.CorrelationIdFilter;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
@SpringBootApplication public class OrderServiceApplication {
 public static void main(String[] args) { SpringApplication.run(OrderServiceApplication.class,args); }
 @Bean CorrelationIdFilter correlationIdFilter(){ return new CorrelationIdFilter(); }
 @Bean TopicExchange shopsphereEvents(){ return new TopicExchange("shopsphere.events",true,false); }
 @Bean Queue orderStockEvents(){ return new Queue("order.stock.events", true); }
 @Bean Queue orderPaymentEvents(){ return new Queue("order.payment.events", true); }
 @Bean Binding stockReservedBinding(Queue orderStockEvents, TopicExchange shopsphereEvents){ return BindingBuilder.bind(orderStockEvents).to(shopsphereEvents).with("stock.reserved"); }
 @Bean Binding paymentCompletedBinding(Queue orderPaymentEvents, TopicExchange shopsphereEvents){ return BindingBuilder.bind(orderPaymentEvents).to(shopsphereEvents).with("payment.completed"); }
 @Bean Binding paymentFailedBinding(Queue orderPaymentEvents, TopicExchange shopsphereEvents){ return BindingBuilder.bind(orderPaymentEvents).to(shopsphereEvents).with("payment.failed"); }
 @Bean Jackson2JsonMessageConverter messageConverter(){ return new Jackson2JsonMessageConverter(); }
}
