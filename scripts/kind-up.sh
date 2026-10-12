#!/usr/bin/env bash
set -euo pipefail
kind create cluster --name shopsphere || true
kubectl apply -k k8s/overlays/local
