package com.shopsphere.inventory;
import com.shopsphere.common.web.CorrelationIdFilter;
import org.springframework.boot.SpringApplication; import org.springframework.boot.autoconfigure.SpringBootApplication; import org.springframework.context.annotation.Bean;
@SpringBootApplication public class InventoryServiceApplication { public static void main(String[] args){SpringApplication.run(InventoryServiceApplication.class,args);} @Bean CorrelationIdFilter correlationIdFilter(){return new CorrelationIdFilter();} }
