$ErrorActionPreference = "Stop"
if (-not (kind get clusters | Select-String -Quiet "^shopsphere$")) {
    kind create cluster --name shopsphere
}
kubectl config use-context kind-shopsphere
kind load docker-image shopsphere-api-gateway:latest --name shopsphere
kubectl apply -k k8s/overlays/local
