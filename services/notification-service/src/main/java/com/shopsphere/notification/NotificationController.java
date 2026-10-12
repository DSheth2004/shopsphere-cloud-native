package com.shopsphere.notification;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationListener notifications;

    NotificationController(NotificationListener notifications) { this.notifications = notifications; }

    @GetMapping
    public List<NotificationListener.Notification> mine(@RequestHeader("X-User-Id") String userId) {
        return notifications.forUser(userId);
    }
}
