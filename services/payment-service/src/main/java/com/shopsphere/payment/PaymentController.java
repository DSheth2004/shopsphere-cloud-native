package com.shopsphere.payment;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Payments are driven by saga events; this endpoint only exposes the mock processor's policy for demos. */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final double failureRate;
    private final BigDecimal cardLimit;

    PaymentController(@Value("${shopsphere.payment.failure-rate:0.0}") double failureRate,
                      @Value("${shopsphere.payment.card-limit:50000}") BigDecimal cardLimit) {
        this.failureRate = failureRate;
        this.cardLimit = cardLimit;
    }

    @GetMapping("/policy")
    public Map<String, Object> policy() { return Map.of("failureRate", failureRate, "cardLimit", cardLimit); }
}
