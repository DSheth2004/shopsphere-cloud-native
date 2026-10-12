package com.shopsphere.payment;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Mock card processor. Transient failures are retried and guarded by a circuit breaker; amounts
 * above the demo card limit are always declined so the compensation path can be shown on demand.
 */
@Component
public class PaymentProcessor {
    private final double failureRate;
    private final BigDecimal cardLimit;

    PaymentProcessor(@Value("${shopsphere.payment.failure-rate:0.0}") double failureRate,
                     @Value("${shopsphere.payment.card-limit:50000}") BigDecimal cardLimit) {
        this.failureRate = failureRate;
        this.cardLimit = cardLimit;
    }

    @Retry(name = "charge")
    @CircuitBreaker(name = "charge", fallbackMethod = "unavailable")
    public Result charge(BigDecimal amount) {
        if (amount.compareTo(cardLimit) > 0) return Result.declined("Card declined: amount exceeds the demo limit of ₹" + cardLimit.toPlainString());
        if (ThreadLocalRandom.current().nextDouble() < failureRate) throw new IllegalStateException("Payment gateway timeout");
        return Result.ok();
    }

    Result unavailable(BigDecimal amount, Throwable error) {
        return Result.declined("Payment service unavailable: " + error.getMessage());
    }

    public record Result(boolean approved, String reason) {
        static Result ok() { return new Result(true, null); }
        static Result declined(String reason) { return new Result(false, reason); }
    }
}
