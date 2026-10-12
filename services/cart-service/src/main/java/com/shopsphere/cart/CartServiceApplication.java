package com.shopsphere.cart;
import com.shopsphere.common.web.CorrelationIdFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
@SpringBootApplication public class CartServiceApplication {
 public static void main(String[] args) { SpringApplication.run(CartServiceApplication.class,args); }
 @Bean CorrelationIdFilter correlationIdFilter() { return new CorrelationIdFilter(); }
}
