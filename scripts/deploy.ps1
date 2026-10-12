param([ValidateSet("local", "aws")][string]$Overlay = "local")
$ErrorActionPreference = "Stop"

$services = @(
    "frontend",
    "api-gateway",
    "auth-service",
    "product-service",
    "inventory-service",
    "cart-service",
    "order-service",
    "payment-service",
    "notification-service"
)

if ($Overlay -eq "local") {
    foreach ($svc in $services + "report-job") {
        Write-Host "Loading shopsphere/$svc`:latest into kind..." -ForegroundColor Cyan
        kind load docker-image "shopsphere/$svc`:latest" --name shopsphere
    }
}

Write-Host "Applying Kubernetes manifests for overlay '$Overlay'..." -ForegroundColor Cyan
kubectl apply -k "k8s/overlays/$Overlay"

Write-Host "Waiting for infrastructure..." -ForegroundColor Cyan
foreach ($infra in "postgres", "mongodb", "redis", "rabbitmq") {
    kubectl -n shopsphere rollout status "deployment/$infra" --timeout=300s
}

Write-Host "Waiting for deployments rollout..." -ForegroundColor Cyan
foreach ($svc in $services) {
    kubectl -n shopsphere rollout status "deployment/$svc" --timeout=300s
}

Write-Host "All ShopSphere deployments rolled out successfully!" -ForegroundColor Green
kubectl -n shopsphere get pods
