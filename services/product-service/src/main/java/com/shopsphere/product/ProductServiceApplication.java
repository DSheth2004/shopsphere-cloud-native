package com.shopsphere.product;

import com.shopsphere.common.web.CorrelationIdFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
@SpringBootApplication public class ProductServiceApplication {
    public static void main(String[] args) { SpringApplication.run(ProductServiceApplication.class, args); }
    @Bean CorrelationIdFilter correlationIdFilter() { return new CorrelationIdFilter(); }
}
