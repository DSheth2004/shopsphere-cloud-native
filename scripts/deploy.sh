#!/usr/bin/env bash
# Apply an overlay (local | aws) and wait for every workload to roll out.
set -euo pipefail
cd "$(dirname "$0")/.."
OVERLAY=${1:-local}
kubectl apply -k "k8s/overlays/$OVERLAY"
for s in postgres mongodb redis rabbitmq; do kubectl -n shopsphere rollout status "deployment/$s" --timeout=300s; done
for d in frontend api-gateway auth-service product-service inventory-service cart-service order-service payment-service notification-service; do
  kubectl -n shopsphere rollout status "deployment/$d" --timeout=300s
done
kubectl -n shopsphere get pods
