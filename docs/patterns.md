# Design patterns

| Pattern | Implementation | Trade-off |
| --- | --- | --- |
| API Gateway | `services/api-gateway/application.yml` | Single edge tier can become a bottleneck |
| Database per service | Compose databases and service ownership table | Cross-service reporting is harder |
| Saga | `OrderController` state transitions/events | Eventual consistency |
| Transactional outbox | `order-service` design boundary; SQL migration added with PostgreSQL profile | Extra table/publisher |
| Pub/Sub | `OrderEvents`, RabbitMQ exchange | Harder tracing |
| Cache-aside | Product/Redis profile | Stale reads |
| CQRS-lite | `ProductController` query vs command endpoints | More code paths |
| Circuit breaker/retry | `PaymentController` Resilience4j annotations | May mask persistent failure |
| Bulkhead | Payment resilience configuration | Capacity tuning needed |
| Idempotency | Inventory reservation ID map | Storage/retention required |
| Health checks | Actuator probes in all service YAML | False positives possible |
| External config | env variables, ConfigMap/Secret | More deployment config |
| Sidecar | planned Fluent Bit sidecar in k8s logging overlay | Extra pod resources |
| Strangler fig | `docs/analysis.md` migration plan | Temporary dual architecture |
