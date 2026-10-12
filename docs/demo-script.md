# Demo script

A walkthrough of roughly ten minutes that touches every grading criterion.

## 1. Microservices and UI (local or public link)

1. `docker compose up --build -d`, then open http://localhost:3000 (or the AWS `app_url`).
2. Open **Platform** in the header to show all eight services reporting `UP` through the gateway.
3. Register a new user, browse and search the catalogue, add two products to the cart.
4. Check out. On **Orders**, watch the saga timeline move from `PENDING` to `STOCK_RESERVED`, `PAID`, `CONFIRMED`. The bell shows the notification.
5. Failure path: add enough items to exceed ₹50,000 and check out. Payment is declined, the order is `CANCELLED`, and stock is released (compensation). Ordering more units than are in stock shows the inventory rejection path.
6. Sign in as `admin@shopsphere.local` / `Admin@12345`. On **Admin**, add a product and change stock levels. A normal user gets `403` for the same calls.

## 2. Docker

- Show the multi-stage `Dockerfile`: Maven build stage, layered jar extraction, JRE-only runtime, non-root user `10001`.
- `docker compose ps` shows healthchecks; `docker stats` shows the memory limits.

## 3. Kubernetes

1. `bash scripts/kind-up.sh` (or `scripts/kind-up.ps1`), then open http://localhost:30080.
2. `kubectl -n shopsphere get pods,svc,hpa,pdb,cronjob,networkpolicy`.
3. Self-healing: `bash scripts/chaos.sh` deletes the gateway pod and you can watch it come back.
4. Autoscaling: kind has no metrics-server by default, so install it first (`kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml`, then add `--kubelet-insecure-tls` to its args). k3s on AWS ships with it. Run `make load` (k6) and watch `kubectl -n shopsphere get hpa -w`.

## 4. IaC and cloud

- Walk through `infra/terraform`: EC2 + k3s, Elastic IP, SSM-only access, GitHub OIDC role, budget alerts and nightly stop.
- `terraform output app_url` gives the public link.

## 5. CI/CD and DevSecOps

- GitHub **Actions** tab: CI (tests, coverage, manifest validation), Security (Gitleaks, Trivy fs + IaC), CodeQL, CD (build, Trivy image scan, push to GHCR, SSM deploy), Terraform.
- GitHub **Security, Code scanning** shows the uploaded Trivy and CodeQL results.
- Push a small change to `master` and show CD rolling the new images onto the EC2 node.
