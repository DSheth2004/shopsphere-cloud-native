package com.shopsphere.order;
import com.shopsphere.common.events.OrderEvents;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Saga orchestrator entry point. Outbox persistence is enabled when the PostgreSQL profile is used. */
@RestController @RequestMapping("/api/orders") public class OrderController {
 private final Map<UUID, Order> orders = new ConcurrentHashMap<>(); private final RabbitTemplate events;
 OrderController(RabbitTemplate events){this.events=events;}
 @PostMapping @ResponseStatus(HttpStatus.ACCEPTED) public Order create(@RequestBody CreateOrder request,@RequestHeader(value="x-request-id",defaultValue="local") String correlationId){
  UUID id=UUID.randomUUID(); Order order=new Order(id,request.userId(),request.total(),Status.PENDING); orders.put(id,order);
  events.convertAndSend("shopsphere.events","order.created",new OrderEvents.OrderCreated(id,request.userId(),request.total(),correlationId,Instant.now())); return order;
 }
 @GetMapping("/{id}") public Order get(@PathVariable UUID id){Order value=orders.get(id);if(value==null)throw new IllegalArgumentException("Order not found");return value;}
 @PostMapping("/{id}/confirm") public Order confirm(@PathVariable UUID id){return transition(id,Status.CONFIRMED);}
 @PostMapping("/{id}/cancel") public Order cancel(@PathVariable UUID id){return transition(id,Status.CANCELLED);}
 Order transition(UUID id,Status status){Order old=get(id);Order next=new Order(id,old.userId(),old.total(),status);orders.put(id,next);return next;}
 public record CreateOrder(String userId,BigDecimal total){} public record Order(UUID id,String userId,BigDecimal total,Status status){} public enum Status{PENDING,STOCK_RESERVED,PAID,CONFIRMED,CANCELLED}
}
