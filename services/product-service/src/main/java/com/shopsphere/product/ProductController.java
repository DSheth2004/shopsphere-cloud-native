package com.shopsphere.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** CQRS-lite boundary: query endpoints are public, command endpoints are admin-only at the gateway. */
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository products;

    ProductController(ProductRepository products) { this.products = products; }

    @GetMapping
    public Collection<Product> search(@RequestParam(required = false) String q, @RequestParam(required = false) String category) {
        if (q != null && !q.isBlank()) return products.findByNameContainingIgnoreCaseOrderByName(q.trim());
        if (category != null && !category.isBlank()) return products.findByCategoryOrderByName(category);
        return products.findAll(Sort.by("name"));
    }

    @GetMapping("/categories")
    public Collection<String> categories() {
        TreeSet<String> names = new TreeSet<>(Comparator.naturalOrder());
        products.findAll().forEach(product -> names.add(product.category()));
        return names;
    }

    @GetMapping("/{id}")
    public Product find(@PathVariable String id) {
        return products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@Valid @RequestBody ProductInput input) {
        String id = "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return products.save(input.toProduct(id));
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable String id, @Valid @RequestBody ProductInput input) {
        find(id);
        return products.save(input.toProduct(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) { products.deleteById(id); }

    public record ProductInput(@NotBlank @Size(max = 80) String name, @Size(max = 400) String description,
                               @NotNull @DecimalMin("0.01") BigDecimal price, @NotBlank String category, String image) {
        Product toProduct(String id) {
            return new Product(id, name.trim(), description == null ? "" : description.trim(), price, category.trim(),
                    image == null || image.isBlank() ? "📦" : image.trim());
        }
    }
}
