package com.shopsphere.cart;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CartControllerTest {
    @Test
    void subtotalAndCountCoverEveryLine() {
        CartController.Cart cart = CartController.Cart.of("u1", List.of(
                new CartController.CartItem("SKU-1", "Mug", new BigDecimal("250.00"), "🍵", 2),
                new CartController.CartItem("SKU-2", "Lamp", new BigDecimal("999.50"), "💡", 1)));
        assertThat(cart.itemCount()).isEqualTo(3);
        assertThat(cart.subtotal()).isEqualByComparingTo("1499.50");
    }

    @Test
    void cartsAreNamespacedPerUser() {
        assertThat(CartController.key("ada@example.com")).isEqualTo("cart:ada@example.com");
    }
}
