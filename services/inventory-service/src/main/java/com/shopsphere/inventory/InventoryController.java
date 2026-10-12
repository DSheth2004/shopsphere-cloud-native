package com.shopsphere.inventory;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

/** Stock queries are public; stock changes are restricted to admins at the gateway. */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final StockLedger ledger;

    InventoryController(StockLedger ledger) { this.ledger = ledger; }

    @GetMapping public Map<String, Integer> all() { return ledger.snapshot(); }

    @GetMapping("/{sku}") public Map<String, Object> one(@PathVariable String sku) {
        return Map.of("sku", sku, "available", ledger.availableFor(sku));
    }

    @PutMapping("/{sku}") public Map<String, Object> set(@PathVariable String sku, @RequestParam int quantity) {
        ledger.set(sku, quantity);
        return one(sku);
    }
}
