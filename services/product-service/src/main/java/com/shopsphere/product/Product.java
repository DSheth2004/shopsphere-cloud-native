package com.shopsphere.product;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Catalogue entry stored in MongoDB; the id doubles as the SKU used by inventory and orders. */
@Document("products")
public record Product(@Id String id, String name, String description, BigDecimal price, String category, String image) { }
