package com.shopsphere.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductControllerTest {
    private final ProductRepository repository = mock(ProductRepository.class);
    private final ProductController controller = new ProductController(repository);

    @Test
    void createAssignsSkuAndDefaultImage() {
        when(repository.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));
        Product created = controller.create(new ProductController.ProductInput(" Lamp ", null, new BigDecimal("10.00"), "Home", ""));
        assertThat(created.id()).startsWith("SKU-");
        assertThat(created.name()).isEqualTo("Lamp");
        assertThat(created.image()).isEqualTo("📦");
    }

    @Test
    void demoCatalogUsesUniqueSkus() {
        assertThat(CatalogSeeder.DEMO_CATALOG).extracting(Product::id).doesNotHaveDuplicates();
    }
}
