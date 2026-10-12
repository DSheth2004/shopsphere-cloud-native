package com.shopsphere.product;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByNameContainingIgnoreCaseOrderByName(String name);
    List<Product> findByCategoryOrderByName(String category);
}
