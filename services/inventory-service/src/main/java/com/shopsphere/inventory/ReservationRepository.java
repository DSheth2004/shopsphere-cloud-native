package com.shopsphere.inventory;

import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

interface ReservationRepository extends MongoRepository<MongoInventoryStore.Reservation, UUID> { }
