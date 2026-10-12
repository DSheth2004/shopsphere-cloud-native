# ShopSphere

ShopSphere is a student-scale, cloud-native e-commerce platform for the CNA DevOps capstone. It uses Java 21, Spring Boot, Maven, Docker, Kubernetes, Terraform, AWS and GitHub Actions. The project is deliberately built in phases so each design choice can be demonstrated in a viva.

## Phase status

The repository now includes a runnable local Compose topology, a kind/Kustomize deployment baseline, Prometheus configuration, GitHub workflow templates, Terraform cost controls, load-test scaffold, and security/reliability documentation.

## Modules

| Module | Current purpose | Planned responsibility |
| --- | --- | --- |
| `common-lib` | Shared DTO/event/error/web primitives | Reused contracts and correlation IDs |
| `auth-service` | Placeholder | Registration, JWT, roles |
| `product-service` | Placeholder | Product catalogue and Redis cache |
| `inventory-service` | Placeholder | Stock reservation |
| `cart-service` | Placeholder | Redis-backed carts |
| `order-service` | Placeholder | Saga orchestration and outbox |
| `payment-service` | Placeholder | Mock payments and resilience |
| `notification-service` | Placeholder | Event-driven notifications |
| `api-gateway` | Placeholder | Routing, auth and rate limits |
| `report-job` | Placeholder | Daily sales batch job |

## Prerequisites

- JDK 21
- Maven 3.9+
- Git

## Verify Phase 0

```bash
mvn -Dmaven.repo.local=.m2 verify
# or
make verify
```

Expected result: Maven reports `BUILD SUCCESS`. Start the local platform with `make up`, then use `http://localhost:8080/actuator/health`. Stop it with `make down`.

## Local smoke test

After all containers are healthy, run:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1
```

It verifies the gateway and sends a unique register, product, cart, and order request.

## Repository conventions

- Java packages use `com.shopsphere`.
- Environment-dependent values are supplied through environment variables; copy `.env.example` to `.env` locally and never commit it.
- All services will use the `x-request-id` header. `common-lib` turns it into the MDC `correlationId` value.
- Service implementation is added only in its scheduled phase to preserve incremental, reviewable commits.

See [the Git workflow](docs/git-workflow.md) for branch, pull-request, commit, and release conventions.
