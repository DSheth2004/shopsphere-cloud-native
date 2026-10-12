package com.shopsphere.common.config;

import com.shopsphere.common.web.CorrelationIdFilter;
import com.shopsphere.common.web.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/** Registers correlation IDs and consistent error bodies in every servlet-based service. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ShopSphereWebAutoConfiguration {
    @Bean CorrelationIdFilter correlationIdFilter() { return new CorrelationIdFilter(); }
    @Bean GlobalExceptionHandler globalExceptionHandler() { return new GlobalExceptionHandler(); }
}
