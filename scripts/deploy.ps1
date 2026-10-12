param([ValidateSet("local", "aws")][string]$Overlay = "local")
$ErrorActionPreference = "Stop"
if ($Overlay -eq "local") {
    kind load docker-image shopsphere-api-gateway:latest --name shopsphere
}
kubectl apply -k "k8s/overlays/$Overlay"
kubectl -n shopsphere rollout status deployment/api-gateway --timeout=180s
