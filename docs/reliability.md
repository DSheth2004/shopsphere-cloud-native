# Reliability and incident response

SLO: 99% of order API responses below 500 ms monthly; availability target 99.5%. Track HTTP error ratio, p95 latency, queue depth, JVM heap, pod restart count, and circuit-breaker state via Prometheus.

If payment is down: confirm the circuit is open, pause nonessential traffic, inspect RabbitMQ retry/DLQ, and let the saga cancel/release stock. Restore payment, replay only idempotent failed events, then verify order states by correlation ID.
