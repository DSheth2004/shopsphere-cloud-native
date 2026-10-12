package com.shopsphere.common.events;

import java.time.Instant;
import java.util.UUID;

/** Base metadata carried by every RabbitMQ domain event. */
public record DomainEvent(
        UUID eventId,
        String eventType,
        String aggregateId,
        String correlationId,
        Instant occurredAt) { }
