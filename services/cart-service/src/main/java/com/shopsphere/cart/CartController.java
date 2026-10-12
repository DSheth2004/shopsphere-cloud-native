package com.shopsphere.cart;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.web.bind.annotation.*;
/** Redis becomes the backing store through the docker/k8s profile; this API remains stateless. */
@RestController @RequestMapping("/api/carts") public class CartController {
 private final Map<String,List<CartItem>> carts = new ConcurrentHashMap<>();
 @GetMapping("/{userId}") public List<CartItem> get(@PathVariable String userId) { return carts.getOrDefault(userId,List.of()); }
 @PostMapping("/{userId}/items") public List<CartItem> add(@PathVariable String userId,@RequestBody CartItem item) { carts.computeIfAbsent(userId,k->new ArrayList<>()).add(item); return get(userId); }
 @DeleteMapping("/{userId}") public void clear(@PathVariable String userId) { carts.remove(userId); }
 public record CartItem(String productId,int quantity) { }
}
