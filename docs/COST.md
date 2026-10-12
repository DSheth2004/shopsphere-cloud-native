# Cost controls

The AWS footprint is deliberately one instance so the $80 credit lasts. Prices are on-demand for `ap-south-1` (Mumbai) and are estimates; check the AWS pricing page before applying.

| Item | Running | Stopped |
| --- | --- | --- |
| EC2 `t3.medium` (k3s node) | about $0.045/h, roughly $1.08/day | $0 |
| Public IPv4 / Elastic IP | about $0.005/h, roughly $0.12/day | still charged, roughly $0.12/day |
| 20 GB gp3 root volume | about $0.06/day | about $0.06/day |
| SSM parameter, IAM, Budgets, EventBridge Scheduler | free tier | free tier |
| Container images | GHCR (free for public packages) | n/a |

Rough totals: about **$1.30 per day while running** and **about $0.20 per day while stopped**. A week left running costs about $9; a month stopped between demos costs about $6.

## What keeps cost down

- No NAT gateway, no load balancer, no EKS control plane, no RDS, no ElastiCache. k3s runs PostgreSQL, MongoDB, Redis and RabbitMQ as pods on the same node, and Traefik (bundled with k3s) serves the public link on port 80.
- Images live on GHCR instead of ECR, so there is no registry storage or transfer bill.
- `nightly_stop_enabled` (default on) stops the instance at 23:00 IST every night. Start it before a demo with `terraform output -raw start_command`. The Elastic IP keeps the link the same across restarts.
- AWS Budget `shopsphere-monthly` (default $80) emails `budget_email` at 37.5% ($30), 75% ($60) and 100% actual spend, and when forecasted spend crosses 100%.
- A 2 GB swap file lets the whole stack fit in a 4 GB `t3.medium`. `t3.small` is cheaper but too small for 13 pods.

## After grading

Run `terraform destroy` in `infra/terraform`. That removes the instance, Elastic IP, volume, IAM roles, budget and schedule, and the bill goes to zero.
