package com.shopsphere.order;
import com.shopsphere.common.web.CorrelationIdFilter;
import org.springframework.amqp.core.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
@SpringBootApplication public class OrderServiceApplication {
 public static void main(String[] args) { SpringApplication.run(OrderServiceApplication.class,args); }
 @Bean CorrelationIdFilter correlationIdFilter(){ return new CorrelationIdFilter(); }
 @Bean TopicExchange shopsphereEvents(){ return new TopicExchange("shopsphere.events",true,false); }
}
