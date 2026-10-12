package com.shopsphere.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/** Carts live in Redis (one hash per user) so any cart-service replica can serve any request. */
@RestController
@RequestMapping("/api/carts")
public class CartController {
    static final Duration CART_TTL = Duration.ofDays(7);
    private final StringRedisTemplate redis;
    private final JsonMapper json;

    CartController(StringRedisTemplate redis, JsonMapper json) {
        this.redis = redis;
        this.json = json;
    }

    @GetMapping
    public Cart get(@RequestHeader("X-User-Id") String userId) {
        HashOperations<String, String, String> hash = redis.opsForHash();
        List<CartItem> items = hash.values(key(userId)).stream().map(this::read)
                .sorted(Comparator.comparing(CartItem::name)).toList();
        return Cart.of(userId, items);
    }

    @PostMapping("/items")
    public Cart add(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody CartItem item) {
        HashOperations<String, String, String> hash = redis.opsForHash();
        String existing = hash.get(key(userId), item.productId());
        int quantity = Math.min(99, item.quantity() + (existing == null ? 0 : read(existing).quantity()));
        hash.put(key(userId), item.productId(), write(item.withQuantity(quantity)));
        redis.expire(key(userId), CART_TTL);
        return get(userId);
    }

    @PutMapping("/items/{productId}")
    public Cart setQuantity(@RequestHeader("X-User-Id") String userId, @PathVariable String productId, @RequestParam int quantity) {
        HashOperations<String, String, String> hash = redis.opsForHash();
        String existing = hash.get(key(userId), productId);
        if (existing != null) {
            if (quantity <= 0) hash.delete(key(userId), productId);
            else hash.put(key(userId), productId, write(read(existing).withQuantity(Math.min(99, quantity))));
        }
        return get(userId);
    }

    @DeleteMapping("/items/{productId}")
    public Cart remove(@RequestHeader("X-User-Id") String userId, @PathVariable String productId) {
        redis.opsForHash().delete(key(userId), productId);
        return get(userId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@RequestHeader("X-User-Id") String userId) { redis.delete(key(userId)); }

    static String key(String userId) { return "cart:" + userId; }

    private CartItem read(String value) {
        try { return json.readValue(value, CartItem.class); }
        catch (JacksonException e) { throw new IllegalStateException("Corrupt cart entry", e); }
    }

    private String write(CartItem item) {
        try { return json.writeValueAsString(item); }
        catch (JacksonException e) { throw new IllegalStateException("Cannot store cart entry", e); }
    }

    public record CartItem(@NotBlank String productId, @NotBlank String name, @NotNull BigDecimal price,
                           String image, @Min(1) @Max(99) int quantity) {
        CartItem withQuantity(int value) { return new CartItem(productId, name, price, image, value); }
        BigDecimal lineTotal() { return price.multiply(BigDecimal.valueOf(quantity)); }
    }

    public record Cart(String userId, List<CartItem> items, int itemCount, BigDecimal subtotal) {
        static Cart of(String userId, List<CartItem> items) {
            return new Cart(userId, items, items.stream().mapToInt(CartItem::quantity).sum(),
                    items.stream().map(CartItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
    }
}
