package com.shopsphere.order;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, UUID> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
}
