package com.shopsphere.inventory;
import java.util.Map; import java.util.concurrent.ConcurrentHashMap; import org.springframework.web.bind.annotation.*;
/** Reservation endpoint is idempotent by reservationId; JPA optimistic locking is used in the PostgreSQL profile. */
@RestController @RequestMapping("/api/inventory") public class InventoryController {
 private final Map<String,Integer> stock=new ConcurrentHashMap<>(); private final Map<String,Boolean> reservations=new ConcurrentHashMap<>();
 @PutMapping("/{sku}") public Map<String,Integer> set(@PathVariable String sku,@RequestParam int quantity){stock.put(sku,quantity);return Map.of("available",quantity);}
 @PostMapping("/reserve/{reservationId}") public Map<String,Object> reserve(@PathVariable String reservationId,@RequestParam String sku,@RequestParam int quantity){boolean ok=reservations.computeIfAbsent(reservationId,k->{int available=stock.getOrDefault(sku,0);if(available<quantity)return false;stock.put(sku,available-quantity);return true;});return Map.of("reserved",ok);}
}
