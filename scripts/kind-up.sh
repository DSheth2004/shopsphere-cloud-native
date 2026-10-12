#!/usr/bin/env bash
# Create a kind cluster, build all images, load them and deploy the local overlay.
# Storefront: http://localhost:30080
set -euo pipefail
cd "$(dirname "$0")/.."
CLUSTER=shopsphere
SERVICES="api-gateway auth-service product-service inventory-service cart-service order-service payment-service notification-service report-job"

kind get clusters | grep -qx "$CLUSTER" || kind create cluster --name "$CLUSTER" --config scripts/kind-config.yaml
kubectl config use-context "kind-$CLUSTER"

docker compose build
for svc in frontend $SERVICES; do
  echo "Loading shopsphere/$svc:latest into kind"
  kind load docker-image "shopsphere/$svc:latest" --name "$CLUSTER"
done

scripts/deploy.sh local
echo "ShopSphere is up at http://localhost:30080"
