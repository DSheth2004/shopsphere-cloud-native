package com.shopsphere.common.config;

import com.shopsphere.common.events.OrderEvents;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/** Shared RabbitMQ topology and JSON serialisation for the saga participants. */
@AutoConfiguration
@ConditionalOnClass(RabbitTemplate.class)
public class ShopSphereMessagingAutoConfiguration {
    @Bean TopicExchange shopsphereEvents() { return new TopicExchange(OrderEvents.EXCHANGE, true, false); }
    @Bean MessageConverter jsonMessageConverter() { return new JacksonJsonMessageConverter("com.shopsphere.common.events"); }
}
