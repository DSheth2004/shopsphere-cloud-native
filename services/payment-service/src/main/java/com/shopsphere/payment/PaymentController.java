package com.shopsphere.payment;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker; import io.github.resilience4j.retry.annotation.Retry; import java.math.BigDecimal; import java.util.Map; import java.util.concurrent.ThreadLocalRandom; import org.springframework.beans.factory.annotation.Value; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/payments") public class PaymentController {
 @Value("${shopsphere.payment.failure-rate:0.0}") double failureRate;
 @PostMapping @CircuitBreaker(name="charge",fallbackMethod="declined") @Retry(name="charge") public Map<String,Object> charge(@RequestBody Charge charge){if(ThreadLocalRandom.current().nextDouble()<failureRate)throw new IllegalStateException("Mock processor declined charge");return Map.of("orderId",charge.orderId(),"status","COMPLETED");}
 Map<String,Object> declined(Charge charge,Throwable error){return Map.of("orderId",charge.orderId(),"status","FAILED","reason",error.getMessage());}
 public record Charge(String orderId,BigDecimal amount){}
}
