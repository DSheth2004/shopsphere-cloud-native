# Monolith vs microservices analysis plan

The baseline monolith will own product/cart/order data in one PostgreSQL database. Run k6 smoke/load/spike/stress scripts against both systems and capture throughput, p50/p95/p99, errors, pod CPU/memory, readiness time, HPA time, payment failure blast radius and single-component deployment time. Microservices gain independent scaling/deployability but add latency, eventual consistency and JVM operational overhead. Populate this report with measured results during the demo; do not invent performance numbers.
