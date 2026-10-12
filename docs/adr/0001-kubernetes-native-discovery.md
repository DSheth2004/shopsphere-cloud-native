# ADR 0001: use Kubernetes DNS in Kubernetes

## Decision

Services use Kubernetes Service/CoreDNS names on Kubernetes rather than Eureka. Docker Compose may use service-name DNS.

## Consequences

This avoids operating a discovery server and follows Kubernetes conventions. It does not demonstrate client-side discovery, so that remains an optional Compose-only extension.
