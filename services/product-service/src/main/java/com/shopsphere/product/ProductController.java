package com.shopsphere.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** CQRS-lite boundary: query endpoints are isolated from product-changing commands. */
@RestController @RequestMapping("/api/products")
public class ProductController {
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    @GetMapping public Collection<Product> search(@RequestParam(required = false) String q) {
        return products.values().stream().filter(p -> q == null || p.name().toLowerCase().contains(q.toLowerCase())).toList();
    }
    @GetMapping("/{id}") public Product find(@PathVariable String id) { return require(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Product create(@Valid @RequestBody ProductInput input) {
        String id = UUID.randomUUID().toString(); Product product = new Product(id, input.name(), input.price(), input.category()); products.put(id, product); return product;
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable String id) { products.remove(id); }
    private Product require(String id) { Product value = products.get(id); if (value == null) throw new IllegalArgumentException("Product not found"); return value; }
    public record Product(String id, String name, BigDecimal price, String category) { }
    public record ProductInput(@NotBlank String name, @NotNull BigDecimal price, @NotBlank String category) { }
}
