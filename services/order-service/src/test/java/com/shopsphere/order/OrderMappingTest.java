package com.shopsphere.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/** The order record, with its nested lines and saga history, must round-trip through MongoDB unchanged. */
class OrderMappingTest {
    private final MappingMongoConverter converter = converter();

    @Test
    void orderRoundTripsThroughTheMongoMapping() {
        Order order = Order.create("ada", List.of(new Order.Line("SKU-1001", "Mug", "🍵", new BigDecimal("250.00"), 2)))
                .transition(Order.Status.STOCK_RESERVED, "Stock reserved")
                .transition(Order.Status.CANCELLED, "Payment declined");

        Document stored = new Document();
        converter.write(order, stored);
        Order loaded = converter.read(Order.class, stored);

        assertThat(stored.get("_id")).isEqualTo(order.id());
        assertThat(loaded).usingRecursiveComparison().withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                // MongoDB dates have millisecond precision.
                .withComparatorForType((a, b) -> a.truncatedTo(ChronoUnit.MILLIS).compareTo(b.truncatedTo(ChronoUnit.MILLIS)), Instant.class)
                .isEqualTo(order);
    }

    private static MappingMongoConverter converter() {
        MongoMappingContext context = new MongoMappingContext();
        MongoCustomConversions conversions = new MongoCustomConversions(List.of());
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        context.afterPropertiesSet();
        MappingMongoConverter converter = new MappingMongoConverter(NoOpDbRefResolver.INSTANCE, context);
        converter.setCustomConversions(conversions);
        converter.afterPropertiesSet();
        return converter;
    }
}
