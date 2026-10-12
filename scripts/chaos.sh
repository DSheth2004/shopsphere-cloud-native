#!/usr/bin/env bash
set -euo pipefail
kubectl -n shopsphere delete pod -l app=api-gateway --wait=false
kubectl -n shopsphere get pods -w
