$ErrorActionPreference = "Stop"
kubectl -n shopsphere delete pod -l app=api-gateway
kubectl -n shopsphere get pods -w
