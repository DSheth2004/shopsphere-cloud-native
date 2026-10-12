package com.shopsphere.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class InventoryControllerTest {
    private final InventoryController controller = new InventoryController(new StockLedger());

    @Test
    void demoCatalogueIsStocked() {
        assertThat(controller.all()).containsEntry("SKU-1001", 25).containsEntry("SKU-1004", 3);
    }

    @Test
    void adminCanSetStock() {
        assertThat(controller.set("SKU-NEW", 7)).containsEntry("available", 7);
        assertThat(controller.one("SKU-NEW")).containsEntry("available", 7);
    }

    @Test
    void negativeStockIsRejected() {
        assertThatThrownBy(() -> controller.set("SKU-1001", -1)).isInstanceOf(IllegalArgumentException.class);
    }
}
