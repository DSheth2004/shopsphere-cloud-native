package com.shopsphere.notification;
import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.springframework.amqp.rabbit.annotation.RabbitListener; import org.springframework.stereotype.Component;
/** Notification is deliberately decoupled from the checkout path. */
@Component public class NotificationListener { private static final Logger log=LoggerFactory.getLogger(NotificationListener.class); @RabbitListener(queues="notification.events") public void notify(Object event){log.info("Sending customer notification for event={}",event);} }
