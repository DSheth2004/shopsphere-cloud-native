package com.shopsphere.notification;

import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<NotificationListener.Notification, UUID> {
    List<NotificationListener.Notification> findTop50ByUserIdOrderByAtDesc(String userId);
}
