#!/usr/bin/env bash
set -euo pipefail
kubectl apply -k "k8s/overlays/${1:-local}"
kubectl -n shopsphere rollout status deployment/api-gateway
