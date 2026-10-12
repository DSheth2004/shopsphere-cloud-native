package com.shopsphere.product;

import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Loads a demo catalogue into an empty database so the storefront is never blank. */
@Component
class CatalogSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);
    static final List<Product> DEMO_CATALOG = List.of(
            new Product("SKU-1001", "Aurora Wireless Headphones", "Over-ear, active noise cancelling, 40h battery.", new BigDecimal("4999.00"), "Electronics", "🎧"),
            new Product("SKU-1002", "Pulse Smartwatch", "AMOLED display, heart-rate and SpO2 tracking.", new BigDecimal("7499.00"), "Electronics", "⌚"),
            new Product("SKU-1003", "Nimbus Mechanical Keyboard", "Hot-swappable switches with per-key RGB.", new BigDecimal("3299.00"), "Electronics", "⌨️"),
            new Product("SKU-1004", "Trailblazer Backpack", "28L water-resistant daypack with laptop sleeve.", new BigDecimal("1899.00"), "Travel", "🎒"),
            new Product("SKU-1005", "Summit Water Bottle", "Insulated steel, keeps drinks cold for 24h.", new BigDecimal("699.00"), "Travel", "🧴"),
            new Product("SKU-1006", "Roast & Co. Coffee Beans", "1kg single-origin Arabica, medium roast.", new BigDecimal("1199.00"), "Grocery", "☕"),
            new Product("SKU-1007", "Zen Ceramic Mug Set", "Set of four hand-glazed 350ml mugs.", new BigDecimal("899.00"), "Home", "🍵"),
            new Product("SKU-1008", "Lumen Desk Lamp", "Dimmable LED with wireless charging base.", new BigDecimal("2499.00"), "Home", "💡"),
            new Product("SKU-1009", "Stride Running Shoes", "Lightweight cushioning for daily runs.", new BigDecimal("3999.00"), "Fashion", "👟"),
            new Product("SKU-1010", "Classic Denim Jacket", "Stonewashed cotton, relaxed fit.", new BigDecimal("2799.00"), "Fashion", "🧥"),
            new Product("SKU-1011", "Cloud Atlas (Paperback)", "David Mitchell's award-winning novel.", new BigDecimal("499.00"), "Books", "📘"),
            new Product("SKU-1012", "Leaf Indoor Planter", "Self-watering pot with a pothos starter.", new BigDecimal("1099.00"), "Home", "🪴"));
    private final ProductRepository products;

    CatalogSeeder(ProductRepository products) { this.products = products; }

    @Override
    public void run(ApplicationArguments args) {
        if (products.count() == 0) {
            products.saveAll(DEMO_CATALOG);
            log.info("Seeded {} demo products", DEMO_CATALOG.size());
        }
    }
}
