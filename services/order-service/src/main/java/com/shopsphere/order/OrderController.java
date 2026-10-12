package com.shopsphere.order;

import com.shopsphere.common.events.OrderEvents;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Saga orchestrator entry point: prices the order from product-service, then publishes order.created. */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderStore store;
    private final RabbitTemplate events;
    private final RestClient products;

    OrderController(OrderStore store, RabbitTemplate events, RestClient productClient) {
        this.store = store;
        this.events = events;
        this.products = productClient;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Order create(@RequestHeader("X-User-Id") String userId, @Valid @RequestBody CreateOrder request,
                        @RequestHeader(value = "x-request-id", defaultValue = "local") String correlationId) {
        List<Order.Line> lines = request.items().stream().map(this::price).toList();
        Order order = store.save(Order.create(userId, lines));
        List<OrderEvents.OrderLine> eventLines = lines.stream()
                .map(line -> new OrderEvents.OrderLine(line.productId(), line.quantity())).toList();
        events.convertAndSend(OrderEvents.EXCHANGE, OrderEvents.ORDER_CREATED,
                new OrderEvents.OrderCreated(order.id(), userId, eventLines, order.total(), correlationId, Instant.now()));
        return order;
    }

    @GetMapping public List<Order> mine(@RequestHeader("X-User-Id") String userId) { return store.forUser(userId); }

    @GetMapping("/{id}")
    public Order get(@RequestHeader("X-User-Id") String userId, @PathVariable UUID id) {
        Order order = store.get(id);
        if (!order.userId().equals(userId)) throw new NoSuchElementException("Order not found");
        return order;
    }

    private Order.Line price(Item item) {
        try {
            ProductView product = products.get().uri("/api/products/{id}", item.productId()).retrieve().body(ProductView.class);
            if (product == null) throw new IllegalArgumentException("Unknown product " + item.productId());
            return new Order.Line(product.id(), product.name(), product.image(), product.price(), item.quantity());
        } catch (HttpClientErrorException.NotFound missing) {
            throw new IllegalArgumentException("Unknown product " + item.productId());
        } catch (RestClientException unavailable) {
            throw new IllegalStateException("Product catalogue is unavailable, please retry");
        }
    }

    public record Item(@NotBlank String productId, @Min(1) @Max(99) int quantity) { }
    public record CreateOrder(@NotEmpty List<@Valid Item> items) { }
    record ProductView(String id, String name, String image, BigDecimal price) { }
}
