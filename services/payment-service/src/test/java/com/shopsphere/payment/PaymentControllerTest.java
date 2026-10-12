package com.shopsphere.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentControllerTest {
    private final PaymentProcessor processor = new PaymentProcessor(0.0, new BigDecimal("50000"));

    @Test
    void approvesAmountsWithinLimit() {
        assertThat(processor.charge(new BigDecimal("4999.00")).approved()).isTrue();
    }

    @Test
    void declinesAmountsAboveLimit() {
        PaymentProcessor.Result result = processor.charge(new BigDecimal("50000.01"));
        assertThat(result.approved()).isFalse();
        assertThat(result.reason()).contains("demo limit");
    }

    @Test
    void policyReportsConfiguredLimits() {
        assertThat(new PaymentController(0.25, new BigDecimal("100")).policy())
                .containsEntry("failureRate", 0.25).containsEntry("cardLimit", new BigDecimal("100"));
    }
}
