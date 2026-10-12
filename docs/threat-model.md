# Threat model (STRIDE)

Trust boundaries are Browser→Ingress, Gateway→services, services→RabbitMQ/datastores, and CI→AWS.

| Threat | Mitigation |
| --- | --- |
| Spoofing | JWT validation, BCrypt passwords, TLS at ingress |
| Tampering | validation, signed CI artifacts, least-privilege IAM |
| Repudiation | `x-request-id`/MDC JSON logs |
| Disclosure | Secrets outside git, restricted actuator endpoints |
| Denial of service | gateway rate limits, HPA, circuit breakers |
| Privilege escalation | role checks, non-root images, NetworkPolicies |
