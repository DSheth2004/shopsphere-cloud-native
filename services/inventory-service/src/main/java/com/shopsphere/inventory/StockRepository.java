package com.shopsphere.inventory;

import org.springframework.data.mongodb.repository.MongoRepository;

interface StockRepository extends MongoRepository<MongoInventoryStore.StockLevel, String> { }
