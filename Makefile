.DEFAULT_GOAL := help

.PHONY: help test verify up down logs smoke kind-up kind-down k8s-validate tf-validate scan load

help:
	@echo "ShopSphere commands"
	@echo "  make test          Run unit tests"
	@echo "  make verify        Run the Maven quality lifecycle (tests + JaCoCo)"
	@echo "  make up            Build and start the full stack (UI on http://localhost:3000)"
	@echo "  make down          Stop the stack and delete volumes"
	@echo "  make smoke         Run the end-to-end smoke test against the stack"
	@echo "  make kind-up       Deploy to a local kind cluster (UI on http://localhost:30080)"
	@echo "  make kind-down     Delete the kind cluster"
	@echo "  make k8s-validate  Render both Kustomize overlays"
	@echo "  make tf-validate   terraform fmt + validate (no AWS calls)"
	@echo "  make scan          Trivy filesystem scan (HIGH/CRITICAL)"
	@echo "  make load          k6 load test against the stack"

test:
	mvn -B test

verify:
	mvn -B verify

up:
	docker compose up --build -d

down:
	docker compose down -v

logs:
	docker compose logs -f --tail=100

smoke:
	bash scripts/smoke-test.sh

kind-up:
	bash scripts/kind-up.sh

kind-down:
	kind delete cluster --name shopsphere

k8s-validate:
	kubectl kustomize k8s/overlays/local > /dev/null
	cp -n k8s/overlays/aws/secrets.env.example k8s/overlays/aws/secrets.env 2>/dev/null || true
	kubectl kustomize k8s/overlays/aws > /dev/null
	@echo "Kustomize overlays render"

tf-validate:
	cd infra/terraform && terraform fmt -check && terraform init -backend=false -input=false && terraform validate

scan:
	trivy fs --severity HIGH,CRITICAL --ignore-unfixed .

load:
	k6 run -e BASE_URL=http://localhost:3000 load-tests/order-flow.js
